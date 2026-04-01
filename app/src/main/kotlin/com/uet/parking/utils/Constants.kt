package com.uet.parking.utils

/**
 * Lớp chứa các hằng số sử dụng trong ứng dụng
 */
object Constants {

    // Time constants
    const val REFRESH_INTERVAL_MS = 30000L // 30 giây
    
    // Parking slot types
    const val SLOT_TYPE_REGULAR = "regular"
    const val SLOT_TYPE_RESERVED = "reserved"
    
    // Parking status
    const val STATUS_AVAILABLE = "AVAILABLE"
    const val STATUS_OCCUPIED = "OCCUPIED"
    const val STATUS_MAINTENANCE = "MAINTENANCE"
    
    // Prefixes
    const val SHARED_PREF_NAME = "uet_parking_prefs"
    const val KEY_LAST_SYNC = "last_sync"
    const val KEY_USER_ID = "user_id"
}
