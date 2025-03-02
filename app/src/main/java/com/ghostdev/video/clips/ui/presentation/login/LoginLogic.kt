package com.ghostdev.video.clips.ui.presentation.login

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostdev.video.clips.data.network.SupabaseClient.client
import com.ghostdev.video.clips.util.SharedPreferencesHelper
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class LoginLogic: ViewModel(), KoinComponent {
    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: MutableStateFlow<LoginUiState> = _loginUiState


    private fun saveToken(
        context: Context
    ) {
        viewModelScope.launch {
            val accessToken = client.auth.currentAccessTokenOrNull() ?: ""
            val sharedPref = SharedPreferencesHelper(context)
            sharedPref.saveStringData("accessToken", accessToken)
        }
    }

    private fun getToken(
        context: Context
    ): String? {
        val sharedPref = SharedPreferencesHelper(context)
        return sharedPref.getStringData("accessToken")
    }

    fun login(
        context: Context,
        userEmail: String,
        userPassword: String
    ) {
        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            _loginUiState.value = loginUiState.value.copy(error = AuthError.FillAllFields)
            return
        } else if (userPassword.length < 8) {
            _loginUiState.value = loginUiState.value.copy(error = AuthError.WeakPassword)
            return
        } else {
            _loginUiState.value = loginUiState.value.copy(loading = true, error = null)
            viewModelScope.launch {
                try {
                    client.auth.signInWith(Email) {
                        email = userEmail
                        password = userPassword
                    }
                    saveToken(context)
                    _loginUiState.value = loginUiState.value.copy(loading = false, error = null, success = true)
                } catch (e: RestException) {
                    val errorType = when {
                        "Invalid login credentials" in e.message.orEmpty() -> AuthError.InvalidLogin
                        "User not found" in e.message.orEmpty() -> AuthError.UserNotFound
                        "Network" in e.message.orEmpty() -> AuthError.NetworkError
                        else -> AuthError.UnknownError
                    }
                    _loginUiState.value = loginUiState.value.copy(error = errorType, loading = false)
                } catch (e: Exception) {
                    _loginUiState.value =
                        loginUiState.value.copy(error = AuthError.UnknownError, loading = false)
                }
            }
        }
    }



    fun isUserLoggedIn(context: Context) {
        _loginUiState.value = loginUiState.value.copy(initialLoading = true)
        viewModelScope.launch {
            try {
                val token = getToken(context)
                if (!token.isNullOrEmpty()) {
                    client.auth.retrieveUser(token)
                    client.auth.refreshCurrentSession()
                    saveToken(context)
                    _loginUiState.value = loginUiState.value.copy(
                        userLoggedIn = true,
                        initialLoading = false,
                        error = null
                    )
                } else {
                    _loginUiState.value = loginUiState.value.copy(
                        userLoggedIn = false,
                        initialLoading = false,
                        error = AuthError.UserNotFound
                    )
                }
            } catch (e: RestException) {
                val errorType = when {
                    "Invalid token" in e.message.orEmpty() -> AuthError.UserNotFound
                    "Network" in e.message.orEmpty() -> AuthError.NetworkError
                    "User does not exist." in e.message.orEmpty() -> AuthError.UserNotFound
                    else -> AuthError.UnknownError
                }
                _loginUiState.value = loginUiState.value.copy(
                    userLoggedIn = false,
                    initialLoading = false,
                    error = errorType
                )
            } catch (e: Exception) {
                _loginUiState.value = loginUiState.value.copy(
                    userLoggedIn = false,
                    initialLoading = false,
                    error = AuthError.UnknownError
                )
            }
        }
    }
}

@Stable
data class LoginUiState(
    val initialLoading: Boolean = true,
    val loading: Boolean = false,
    val userLoggedIn: Boolean = false,
    val error: AuthError? = null,
    val success: Boolean = false
)

sealed class AuthError(val message: String) {
    data object NetworkError : AuthError("Network connection failed.")
    data object FillAllFields : AuthError("Please fill in all fields.")
    data object WeakPassword : AuthError("Password must be at least 8 characters.")
    data object InvalidEmail : AuthError("Please enter a valid email.")
    data object UserNotFound : AuthError("User does not exist.")
    data object InvalidLogin : AuthError("invalid login credentials")
    data object UnknownError : AuthError("Something went wrong. Please try again.")

    data class CustomError(val customMessage: String) : AuthError(customMessage)
}
