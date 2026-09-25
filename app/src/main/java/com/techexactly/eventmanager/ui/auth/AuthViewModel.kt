package com.techexactly.eventmanager.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techexactly.eventmanager.data.repository.AuthRepository
import com.techexactly.eventmanager.util.Resource
import com.techexactly.eventmanager.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthSuccess { LOGIN, REGISTER, RESET_EMAIL_SENT }

data class AuthUiState(
    val loading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val errorMessage: String? = null,   // Firebase / general error (shown once)
    val success: AuthSuccess? = null    // one-shot success event
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    val isLoggedIn: Boolean get() = repository.isLoggedIn

    fun login(email: String, password: String) {
        val emailError = Validators.validateEmail(email)
        val passwordError = if (password.isEmpty()) "Password is required" else null
        if (emailError != null || passwordError != null) {
            _state.value = AuthUiState(emailError = emailError, passwordError = passwordError)
            return
        }
        launchAuth(AuthSuccess.LOGIN) { repository.login(email.trim(), password) }
    }

    fun register(email: String, password: String, confirm: String) {
        val emailError = Validators.validateEmail(email)
        val passwordError = Validators.validatePassword(password)
        val confirmError = Validators.validateConfirmPassword(password, confirm)
        if (emailError != null || passwordError != null || confirmError != null) {
            _state.value = AuthUiState(
                emailError = emailError, passwordError = passwordError, confirmError = confirmError
            )
            return
        }
        launchAuth(AuthSuccess.REGISTER) { repository.register(email.trim(), password) }
    }

    fun resetPassword(email: String) {
        val emailError = Validators.validateEmail(email)
        if (emailError != null) {
            _state.value = AuthUiState(emailError = emailError)
            return
        }
        launchAuth(AuthSuccess.RESET_EMAIL_SENT) { repository.sendPasswordReset(email.trim()) }
    }

    /** Call after the UI has reacted to [AuthUiState.success] / [AuthUiState.errorMessage]. */
    fun onEventHandled() = _state.update { it.copy(errorMessage = null, success = null) }

    private fun launchAuth(onSuccess: AuthSuccess, call: suspend () -> Resource<Unit>) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            _state.value = when (val result = call()) {
                is Resource.Success -> AuthUiState(success = onSuccess)
                is Resource.Error -> AuthUiState(errorMessage = result.message)
                Resource.Loading -> AuthUiState(loading = true)
            }
        }
    }
}
