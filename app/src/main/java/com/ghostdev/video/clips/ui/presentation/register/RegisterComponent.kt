package com.ghostdev.video.clips.ui.presentation.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghostdev.video.clips.ui.components.LoadingScreen
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterComponent(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    navigateToLogin: () -> Unit = {},
    viewmodel: RegisterLogic = koinViewModel()
) {
    val state = viewmodel.registerUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(key1 = state.value) {
        if (state.value.success) {
            navigateToLogin()
        }
    }

    if (state.value.initialLoading) {
        LoadingScreen()
    } else {
        RegisterScreen(
            modifier = modifier,
            isDarkTheme = isDarkTheme,
            navigateToLogin = navigateToLogin,
            register = { email, password, displayName ->
                viewmodel.register(
                    context,
                    email,
                    password,
                    displayName
                )
            },
            loading = state.value.loading,
            error = state.value.error?.message ?: ""
        )
    }
}

@Composable
private fun RegisterScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    navigateToLogin: () -> Unit = {},
    register: (String, String, String) -> Unit = { _, _ , _-> },
    loading: Boolean = false,
    error: String = ""
) {
    val displayName = remember { mutableStateOf("") }
    val emailText = remember { mutableStateOf("") }
    val passwordText = remember { mutableStateOf("") }
    val passwordConfirmText = remember { mutableStateOf("") }
    val errorState = remember { mutableStateOf("") }

    LaunchedEffect(error) {
        if (error.isNotEmpty()) {
            errorState.value = error
            delay(5000)
            errorState.value = ""
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Error message display
        if (error.isNotEmpty() && error != "User does not exist.") {
            AnimatedVisibility(
                visible = errorState.value.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) Color.White else Color.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email Field
            OutlinedTextField(
                value = displayName.value,
                onValueChange = {
                    displayName.value = it
                    errorState.value = ""
                },
                label = { Text("User name", color = Color.Gray) },
                textStyle = TextStyle(color = if (isDarkTheme) Color.White else Color.Black),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = if (isDarkTheme) Color.White else Color(0xFF07A7F2)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = emailText.value,
                onValueChange = {
                    emailText.value = it
                    errorState.value = "" // Clear error on input change
                },
                label = { Text("Email", color = Color.Gray) },
                textStyle = TextStyle(color = if (isDarkTheme) Color.White else Color.Black),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = if (isDarkTheme) Color.White else Color(0xFF07A7F2)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = passwordText.value,
                onValueChange = {
                    passwordText.value = it
                    errorState.value = "" // Clear error on input change
                },
                label = { Text("Password", color = Color.Gray) },
                textStyle = TextStyle(color = if (isDarkTheme) Color.White else Color.Black),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = if (isDarkTheme) Color.White else Color(0xFF07A7F2)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Confirm Field
            OutlinedTextField(
                value = passwordConfirmText.value,
                onValueChange = {
                    passwordConfirmText.value = it
                    errorState.value = "" // Clear error on input change
                },
                label = { Text("Confirm Password", color = Color.Gray) },
                textStyle = TextStyle(color = if (isDarkTheme) Color.White else Color.Black),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = if (isDarkTheme) Color.White else Color(0xFF07A7F2)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Up Button
            Button(
                onClick = {
                    // Validate inputs before submitting
                    when {
                        emailText.value.isEmpty() || passwordText.value.isEmpty() || passwordConfirmText.value.isEmpty() -> {
                            errorState.value = "Please fill in all fields."
                        }
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(emailText.value).matches() -> {
                            errorState.value = "Please enter a valid email."
                        }
                        passwordText.value.length < 8 -> {
                            errorState.value = "Password must be at least 8 characters."
                        }
                        passwordText.value != passwordConfirmText.value -> {
                            errorState.value = "Passwords don't match."
                        }
                        else -> {
                            register(emailText.value, passwordText.value, displayName.value)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFF07A7F2)),
                enabled = !loading
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Create account", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign In Link
            Row {
                Text(text = "Have an account? ", color = Color.Gray, fontSize = 14.sp)
                Text(
                    text = "Sign in here!",
                    color = Color(0xFF07A7F2),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navigateToLogin()
                    }
                )
            }
        }
    }
}