package com.uet.parking.ui.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.Slot
import com.uet.parking.utils.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

class NavigationViewModel : ViewModel() {

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private val _destinationSlot = MutableStateFlow<Slot?>(null)
    val destinationSlot = _destinationSlot.asStateFlow()

    private val _distanceToSlot = MutableStateFlow<Double?>(null)
    val distanceToSlot = _distanceToSlot.asStateFlow()

    private val _etaMinutes = MutableStateFlow<Int?>(null)
    val etaMinutes = _etaMinutes.asStateFlow()

    private val _isTracking = MutableStateFlow(true)
    val isTracking = _isTracking.asStateFlow()

    private val _routePoints = MutableStateFlow<List<GeoPoint>>(emptyList())
    val routePoints = _routePoints.asStateFlow()

    private val _currentInstruction = MutableStateFlow<String?>(null)
    val currentInstruction = _currentInstruction.asStateFlow()

    private var lastFetchLocation: Location? = null

    fun setDestination(slot: Slot) {
        _destinationSlot.value = slot
    }

    fun setTracking(tracking: Boolean) {
        _isTracking.value = tracking
    }

    fun startLocationTracking(context: Context) {
        viewModelScope.launch {
            LocationHelper.getLocationUpdates(context).collect { location ->
                _currentLocation.value = location
                
                val dest = _destinationSlot.value
                if (dest?.latitude != null && dest.longitude != null) {
                    val dist = LocationHelper.calculateDistance(
                        lat1 = location.latitude,
                        lon1 = location.longitude,
                        lat2 = dest.latitude,
                        lon2 = dest.longitude
                    )
                    _distanceToSlot.value = dist
                    
                    // Tính thời gian đi bộ (Trung bình 1.4 m/s -> ~84m / phút)
                    _etaMinutes.value = (dist / 84.0).roundToInt().coerceAtLeast(1)

                    // Gọi API vẽ đường nếu chưa gọi, hoặc nếu người dùng đã di chuyển hơn 15 mét so với lần lấy trước
                    val distFromLastFetch = if (lastFetchLocation != null) {
                        LocationHelper.calculateDistance(
                            location.latitude, location.longitude,
                            lastFetchLocation!!.latitude, lastFetchLocation!!.longitude
                        )
                    } else { Double.MAX_VALUE }

                    if (distFromLastFetch > 15.0) {
                        lastFetchLocation = location
                        fetchRouteFromOSRM(location.latitude, location.longitude, dest.latitude, dest.longitude)
                    }
                }
            }
        }
    }

    private fun fetchRouteFromOSRM(startLat: Double, startLon: Double, endLat: Double, endLon: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Sử dụng API tìm đường cho người đi bộ (foot) của Project OSRM, yêu cầu trả về geojson và chi tiết bước (steps)
                val urlString = "http://router.project-osrm.org/route/v1/foot/$startLon,$startLat;$endLon,$endLat?overview=full&geometries=geojson&steps=true"
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "UETParkingApp/1.0")

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    val routes = json.optJSONArray("routes")
                    
                    if (routes != null && routes.length() > 0) {
                        val route = routes.getJSONObject(0)
                        
                        // Parse tọa độ để vẽ đường uốn lượn
                        val geometry = route.getJSONObject("geometry")
                        val coordinates = geometry.getJSONArray("coordinates")
                        val points = mutableListOf<GeoPoint>()
                        for (i in 0 until coordinates.length()) {
                            val coord = coordinates.getJSONArray(i)
                            // OSRM trả về mảng [longitude, latitude]
                            val lon = coord.getDouble(0)
                            val lat = coord.getDouble(1)
                            points.add(GeoPoint(lat, lon))
                        }
                        
                        // Cập nhật State cho View vẽ Polyline
                        _routePoints.value = points

                        // Parse bước rẽ tiếp theo (Turn-by-turn instruction)
                        val legs = route.optJSONArray("legs")
                        if (legs != null && legs.length() > 0) {
                            val leg = legs.getJSONObject(0)
                            val steps = leg.optJSONArray("steps")
                            if (steps != null && steps.length() > 1) {
                                // step 0 thường là depart, ta lấy step 1 là bước di chuyển tiếp theo
                                val nextStep = steps.getJSONObject(1)
                                val maneuver = nextStep.optJSONObject("maneuver")
                                val type = maneuver?.optString("type") ?: ""
                                val modifier = maneuver?.optString("modifier") ?: ""
                                val streetName = nextStep.optString("name", "đường nội khu")
                                
                                val instruction = mapOSRMInstructionToVietnamese(type, modifier, streetName)
                                _currentInstruction.value = instruction
                            } else {
                                _currentInstruction.value = "Đi thẳng tới điểm đến"
                            }
                        }
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun mapOSRMInstructionToVietnamese(type: String, modifier: String, street: String): String {
        val streetName = if (street.isNotBlank()) "vào $street" else "vào đường nội khu"
        return when {
            type == "turn" && modifier.contains("left") -> "↩️ Rẽ trái $streetName"
            type == "turn" && modifier.contains("right") -> "↪️ Rẽ phải $streetName"
            type == "arrive" -> "📍 Bạn sắp đến nơi"
            modifier.contains("straight") || type == "continue" -> "⬆️ Tiếp tục đi thẳng $streetName"
            modifier.contains("slight left") -> "↖️ Chếch sang trái $streetName"
            modifier.contains("slight right") -> "↗️ Chếch sang phải $streetName"
            else -> "⬆️ Đi theo tuyến đường chỉ dẫn"
        }
    }
}
