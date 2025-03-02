package com.ghostdev.video.clips.ui.presentation.register

import android.content.Context
import android.util.Patterns
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostdev.video.clips.data.network.SupabaseClient
import com.ghostdev.video.clips.util.SharedPreferencesHelper
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class RegisterLogic: ViewModel(), KoinComponent {
    private val _registerUiState = MutableStateFlow(RegisterUiState())
    val registerUiState: MutableStateFlow<RegisterUiState> = _registerUiState


    private fun saveToken(
        context: Context
    ) {
        viewModelScope.launch {
            val accessToken = SupabaseClient.client.auth.currentAccessTokenOrNull() ?: ""
            val sharedPref = SharedPreferencesHelper(context)
            sharedPref.saveStringData("accessToken", accessToken)
        }
    }

    fun register(
        context: Context,
        userEmail: String,
        userPassword: String,
        displayName: String
    ) {
        when {
            userEmail.isEmpty() || userPassword.isEmpty() || displayName.isEmpty() -> {
                _registerUiState.value = registerUiState.value.copy(
                    error = AuthError.FillAllFields,
                    loading = false
                )
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches() -> {
                _registerUiState.value = registerUiState.value.copy(
                    error = AuthError.InvalidEmail,
                    loading = false
                )
                return
            }
            userPassword.length < 8 -> {
                _registerUiState.value = registerUiState.value.copy(
                    error = AuthError.WeakPassword,
                    loading = false
                )
                return
            }
        }

        _registerUiState.value = registerUiState.value.copy(loading = true)

        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signUpWith(Email) {
                    email = userEmail
                    password = userPassword
                }

                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: throw Exception("User ID not found")

                SupabaseClient.client.postgrest["profiles"].insert(
                    mapOf(
                        "id" to userId,
                        "display_name" to displayName
                    )
                )

                saveToken(context)
                _registerUiState.value = registerUiState.value.copy(
                    loading = false,
                    success = true,
                    error = null
                )
            } catch (e: Exception) {
                val errorType = when {
                    "Network" in e.message.orEmpty() -> AuthError.NetworkError
                    "already registered" in e.message.orEmpty() -> AuthError.CustomError("This email is already registered.")
                    else -> AuthError.UnknownError
                }

                _registerUiState.value = registerUiState.value.copy(
                    loading = false,
                    error = errorType
                )

                println(e.message)
            }
        }
    }

}

@Stable
data class RegisterUiState(
    val initialLoading: Boolean = false,
    val loading: Boolean = false,
    val error: AuthError? = null,
    val success: Boolean = false
)

sealed class AuthError(val message: String) {
    data object NetworkError : AuthError("Network connection failed.")
    data object FillAllFields : AuthError("Please fill in all fields.")
    data object WeakPassword : AuthError("Password must be at least 8 characters.")
    data object InvalidEmail : AuthError("Please enter a valid email.")
    data object UnknownError : AuthError("Something went wrong. Please try again.")

    data class CustomError(val customMessage: String) : AuthError(customMessage)
}