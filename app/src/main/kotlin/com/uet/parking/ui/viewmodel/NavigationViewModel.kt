package com.uet.parking.ui.viewmodel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.Slot
import com.uet.parking.utils.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class NavigationViewModel : ViewModel(), SensorEventListener {

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private val _destinationSlot = MutableStateFlow<Slot?>(null)
    val destinationSlot = _destinationSlot.asStateFlow()

    private val _distanceToSlot = MutableStateFlow<Double?>(null)
    val distanceToSlot = _distanceToSlot.asStateFlow()

    private val _azimuth = MutableStateFlow(0f)
    val azimuth = _azimuth.asStateFlow()

    private val _bearingToSlot = MutableStateFlow(0f)
    val bearingToSlot = _bearingToSlot.asStateFlow()

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private var isTrackingStarted = false

    fun setDestination(slot: Slot) {
        _destinationSlot.value = slot
    }

    fun startTracking(context: Context) {
        if (isTrackingStarted) return
        isTrackingStarted = true
        startLocationTracking(context)
        startSensorTracking(context)
    }

    private fun startLocationTracking(context: Context) {
        viewModelScope.launch {
            try {
                LocationHelper.getLocationUpdates(context).collect { location ->
                    _currentLocation.value = location
                    
                    val dest = _destinationSlot.value
                    val destLat = dest?.latitude ?: 21.0382
                    val destLon = dest?.longitude ?: 105.7827
                    
                    val dist = LocationHelper.calculateDistance(
                        lat1 = location.latitude,
                        lon1 = location.longitude,
                        lat2 = destLat,
                        lon2 = destLon
                    )
                    _distanceToSlot.value = dist
                    _bearingToSlot.value = calculateBearing(
                        location.latitude, location.longitude,
                        destLat, destLon
                    ).toFloat()
                }
            } catch (e: Exception) {

            }
        }
    }

    private fun startSensorTracking(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        accelerometer?.let {
            val success = sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            val success = sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, event.values.size)
            hasGravity = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
            hasGeomagnetic = true
        }

        if (hasGravity && hasGeomagnetic) {
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                val azimuthRad = orientation[0]
                _azimuth.value = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        
        var bearing = atan2(y, x)
        bearing = Math.toDegrees(bearing)
        return (bearing + 360) % 360
    }
}
