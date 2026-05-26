package com.uet.parking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.UserWithProfile
import com.uet.parking.data.repository.ParkingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale

class PaymentViewModel(
    private val repository: ParkingRepository,
    private val userId: String
) : ViewModel() {

    val userProfile: StateFlow<UserWithProfile?> =
        repository.getUserWithProfile(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    fun buildVnpayMockQrUrl(amount: Double): String {
        val amountInt = amount.toInt()
        val info = URLEncoder.encode("VNPAY UET PARKING $userId $amountInt", "UTF-8")

        return "https://api.qrserver.com/v1/create-qr-code/" +
                "?size=350x350" +
                "&data=$info"
    }

    fun confirmVnpayPayment(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateDebt(userId, 0.0)
            onSuccess()
        }
    }

    fun confirmPayment(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateDebt(userId, 0.0)
            onSuccess()
        }
    }
}

fun Double.toVndText(): String {
    return NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(this)
}

class PaymentViewModelFactory(
    private val repository: ParkingRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
