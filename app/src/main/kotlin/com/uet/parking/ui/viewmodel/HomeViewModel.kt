package com.uet.parking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.UserWithProfile
import com.uet.parking.data.repository.ParkingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class PaymentUiState(
    val showDialog: Boolean = false,
    val isSuccess: Boolean = false,
    val message: String = ""
)

class HomeViewModel(
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

    private val _paymentUiState = MutableStateFlow(PaymentUiState())
    val paymentUiState: StateFlow<PaymentUiState> = _paymentUiState

    private val mockStudentBalance = 50_000.0

    fun payDebt(debt: Double) {
        if (debt <= 0.0) {
            _paymentUiState.update {
                PaymentUiState(
                    showDialog = true,
                    isSuccess = false,
                    message = "Bạn không có khoản nợ cần thanh toán."
                )
            }
            return
        }

        val success = mockStudentBalance >= debt

        _paymentUiState.update {
            PaymentUiState(
                showDialog = true,
                isSuccess = success,
                message = if (success) {
                    "Thanh toán thành công khoản nợ ${debt.toVnd()}."
                } else {
                    "Thanh toán thất bại. Số dư ví mock không đủ."
                }
            )
        }
    }

    fun dismissPaymentDialog() {
        _paymentUiState.update {
            it.copy(showDialog = false)
        }
    }
}

private fun Double.toVnd(): String {
    return java.text.NumberFormat
        .getCurrencyInstance(java.util.Locale("vi", "VN"))
        .format(this)
}

class HomeViewModelFactory(
    private val repository: ParkingRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}