package com.example.icecream.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.icecream.R
import com.example.icecream.repositories.Resource
import com.example.icecream.navigation.Screens
import com.example.icecream.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel?,
    navController: NavController
) {
    val loginFlow = viewModel?.loginFlow?.collectAsState()
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    val isEmailError = remember { mutableStateOf(false) }
    val emailErrorText = remember { mutableStateOf("") }

    val isPasswordError = remember { mutableStateOf(false) }
    val passwordErrorText = remember { mutableStateOf("") }

    val isError = remember { mutableStateOf(false) }
    val errorText = remember { mutableStateOf("") }

    val buttonIsEnabled = remember { mutableStateOf(true) }
    val isLoading = remember { mutableStateOf(false) }

    // Wrap everything in a Box to set the background image
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.sladoled),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Scale the image to fill the screen
        )

        // Foreground content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.welcome_text),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.paddingFromBaseline(top = 32.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = stringResource(id = R.string.login_text),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(20.dp))
            if (isError.value) {
                Text(
                    text = errorText.value,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(id = R.string.email_input_text),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text(stringResource(id = R.string.email_example)) },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                isError = isEmailError.value,
                modifier = Modifier.fillMaxWidth()
            )
            if (isEmailError.value) {
                Text(
                    text = emailErrorText.value,
                    color = Color.Red,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(id = R.string.password_input_text),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text(stringResource(id = R.string.password_example)) },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                isError = isPasswordError.value,
                modifier = Modifier.fillMaxWidth()
            )
            if (isPasswordError.value) {
                Text(
                    text = passwordErrorText.value,
                    color = Color.Red,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(50.dp))
            Button(
                onClick = {
                    isEmailError.value = false
                    isPasswordError.value = false
                    isError.value = false
                    isLoading.value = true
                    viewModel?.login(email.value, password.value)
                },
                enabled = buttonIsEnabled.value,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading.value) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(stringResource(id = R.string.login_button))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(
                onClick = { navController.navigate(Screens.registerScreen) }
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Nemaš nalog? ")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("Registruj se")
                        }
                    }
                )
            }
        }
    }

    loginFlow?.value.let {
        when (it) {
            is Resource.Failure -> {
                isLoading.value = false
                Log.d("Error", it.exception.message.toString())

                when (it.exception.message?.contains("empty", ignoreCase = true)) {
                    true -> {
                        isEmailError.value = true
                        isPasswordError.value = true
                    }
                    else -> {}
                }

                when (it.exception.message?.contains("badly formatted", ignoreCase = true)) {
                    true -> {
                        isEmailError.value = true
                        emailErrorText.value = stringResource(id = R.string.email_badly_formatted)
                    }
                    else -> {}
                }

                when (it.exception.message?.contains("invalid credential", ignoreCase = true)) {
                    true -> {
                        isError.value = true
                        errorText.value = stringResource(id = R.string.credentials_error)
                    }

                    else -> {}

                }
            }
            is Resource.loading -> {
                // Do nothing, as isLoading is already set in onClick
            }
            is Resource.Success -> {
                isLoading.value = false
                LaunchedEffect(Unit) {
                    navController.navigate(Screens.homeScreen) {
                        popUpTo(Screens.homeScreen) {
                            inclusive = true
                        }
                    }
                }
            }
            null -> {}
            else -> {}
        }
    }
}
