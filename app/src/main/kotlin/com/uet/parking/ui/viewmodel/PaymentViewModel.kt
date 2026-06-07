package com.uet.parking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.UserWithProfile
import com.uet.parking.data.repository.ParkingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PaymentState {
    IDLE,
    PROCESSING,
    SUCCESS,
    ERROR
}

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

    private val _paymentState = MutableStateFlow(PaymentState.IDLE)
    val paymentState = _paymentState.asStateFlow()

    private val _transactionId = MutableStateFlow<String?>(null)
    val transactionId = _transactionId.asStateFlow()

    private val _paidAmount = MutableStateFlow(0.0)
    val paidAmount = _paidAmount.asStateFlow()

    fun buildVnpayQrData(amount: Double): String {
        val amountInt = amount.toLong()
        val txnRef = "UETPKG${System.currentTimeMillis() % 1000000}"
        val info = "VNPAY|$txnRef|$amountInt|UETPARKING|$userId"
        return "https://api.qrserver.com/v1/create-qr-code/" +
                "?size=400x400" +
                "&data=${URLEncoder.encode(info, "UTF-8")}" +
                "&color=0A1F44" +
                "&bgcolor=FFFFFF"
    }

    fun confirmPayment(debt: Double) {
        if (_paymentState.value == PaymentState.PROCESSING) return
        
        viewModelScope.launch {
            _paymentState.value = PaymentState.PROCESSING
            _paidAmount.value = debt

            delay(2500)

            try {
                repository.updateDebt(userId, 0.0)

                val txnId = "VNP${SimpleDateFormat("yyMMddHHmmss", Locale.getDefault()).format(Date())}${(1000..9999).random()}"
                _transactionId.value = txnId

                _paymentState.value = PaymentState.SUCCESS
            } catch (e: Exception) {
                _paymentState.value = PaymentState.ERROR
            }
        }
    }

    fun resetPayment() {
        _paymentState.value = PaymentState.IDLE
        _transactionId.value = null
        _paidAmount.value = 0.0
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
