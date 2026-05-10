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
    private val userId: Int
) : ViewModel() {

    val userProfile: StateFlow<UserWithProfile?> =
        repository.getUserWithProfile(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    fun buildVietQrUrl(amount: Double): String {
        val bankId = "MB"
        val accountNo = "0123456789"
        val accountName = URLEncoder.encode("UET PARKING", "UTF-8")
        val addInfo = URLEncoder.encode("UETPARKING$userId", "UTF-8")
        val amountInt = amount.toInt()

        return "https://img.vietqr.io/image/$bankId-$accountNo-compact2.png" +
                "?amount=$amountInt" +
                "&addInfo=$addInfo" +
                "&accountName=$accountName"
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
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}