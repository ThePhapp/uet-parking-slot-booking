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

class NavigationViewModel : ViewModel() {

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private val _destinationSlot = MutableStateFlow<Slot?>(null)
    val destinationSlot = _destinationSlot.asStateFlow()

    private val _distanceToSlot = MutableStateFlow<Double?>(null)
    val distanceToSlot = _distanceToSlot.asStateFlow()

    fun setDestination(slot: Slot) {
        _destinationSlot.value = slot
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
                }
            }
        }
    }
}
