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
import kotlinx.coroutines.launch

data class PaymentUiState(
    val showDialog: Boolean = false,
    val isSuccess: Boolean = false,
    val message: String = ""
)

class HomeViewModel(
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

    private val _paymentUiState = MutableStateFlow(PaymentUiState())
    val paymentUiState: StateFlow<PaymentUiState> = _paymentUiState

    private val mockStudentBalance = 100_000.0

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

        viewModelScope.launch {
            try {
                if (mockStudentBalance >= debt) {
                    repository.updateDebt(userId, 0.0)
                    _paymentUiState.update {
                        PaymentUiState(
                            showDialog = true,
                            isSuccess = true,
                            message = "Thanh toán thành công ${debt.toVnd()}!"
                        )
                    }
                } else {
                    _paymentUiState.update {
                        PaymentUiState(
                            showDialog = true,
                            isSuccess = false,
                            message = "Số dư không đủ để thanh toán."
                        )
                    }
                }
            } catch (e: Exception) {
                _paymentUiState.update {
                    PaymentUiState(
                        showDialog = true,
                        isSuccess = false,
                        message = "Lỗi thanh toán: ${e.message}"
                    )
                }
            }
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
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
