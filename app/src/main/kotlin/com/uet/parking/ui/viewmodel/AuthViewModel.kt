package com.uet.parking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uet.parking.data.model.User
import com.uet.parking.data.model.UserInfo
import com.uet.parking.data.model.enums.UserRole
import com.uet.parking.data.repository.ParkingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: ParkingRepository) : ViewModel() {

    private val _errorText = MutableStateFlow("")
    val errorText = _errorText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun login(email: String, password: String, onSuccess: (String, UserRole) -> Unit) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || password.isEmpty()) {
            _errorText.value = "Vui lòng điền đầy đủ thông tin"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorText.value = ""
            try {
                val user = repository.getUserByEmail(trimmedEmail)
                if (user != null && user.password == password) {
                    onSuccess(user.userId ?: "", user.role)
                } else {
                    _errorText.value = "Sai tài khoản hoặc mật khẩu"
                }
            } catch (e: Exception) {
                _errorText.value = "Lỗi hệ thống: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(fullName: String, email: String, password: String, confirmPassword: String, onSuccess: () -> Unit) {
        val trimmedEmail = email.trim()
        val trimmedName = fullName.trim()
        
        if (trimmedEmail.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || trimmedName.isEmpty()) {
            _errorText.value = "Vui lòng điền đầy đủ thông tin"
            return
        }
        if (!trimmedEmail.endsWith("@vnu.edu.vn")) {
            _errorText.value = "Email phải có đuôi @vnu.edu.vn"
            return
        }
        if (password != confirmPassword) {
            _errorText.value = "Mật khẩu nhập lại không khớp"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorText.value = ""
            try {
                val existingUser = repository.getUserByEmail(trimmedEmail)
                if (existingUser != null) {
                    _errorText.value = "Email này đã được đăng ký"
                } else {
                    val newUser = User(
                        email = trimmedEmail,
                        password = password,
                        name = trimmedName,
                        role = UserRole.USER
                    )
                    // Firestore trả về String ID
                    val userId = repository.createUser(newUser)
                    
                    // Khởi tạo thông tin nợ
                    repository.createUserInfo(UserInfo(userId = userId, debt = 0.0))

                    onSuccess()
                }
            } catch (e: Exception) {
                _errorText.value = "Lỗi đăng ký: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorText.value = ""
    }
}

class AuthViewModelFactory(private val repository: ParkingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
