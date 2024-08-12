package com.example.icecream.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.icecream.R
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.icecream.navigation.Screens
import com.example.icecream.repositories.Resource
import com.example.icecream.viewmodels.AuthViewModel




@Composable
fun RegisterScreen(
    viewModel: AuthViewModel?,
    navController: NavController?
) {
    val registerFlow = viewModel?.registerFlow?.collectAsState()

    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val fullName = remember { mutableStateOf("") }
    val phoneNumber = remember { mutableStateOf("") }
    val profileImage = remember { mutableStateOf(Uri.EMPTY) }

    val isEmailError = remember { mutableStateOf(false) }
    val emailErrorText = remember { mutableStateOf("") }

    val isPasswordError = remember { mutableStateOf(false) }
    val passwordErrorText = remember { mutableStateOf("") }

    val isImageError = remember { mutableStateOf(false) }
    val isFullNameError = remember { mutableStateOf(false) }
    val isPhoneNumberError = remember { mutableStateOf(false) }

    val isError = remember { mutableStateOf(false) }
    val errorText = remember { mutableStateOf("") }

    val buttonIsEnabled = remember { mutableStateOf(true) }
    val isLoading = remember { mutableStateOf(false) }

    // Set up the UI
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.sladoled), // Replace with your image resource
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Foreground content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RegisterImage(profileImage, isImageError)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(id = R.string.register),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.paddingFromBaseline(top = 32.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = stringResource(id = R.string.register_text),
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
                text = stringResource(id = R.string.full_name_text),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = fullName.value,
                onValueChange = { fullName.value = it },
                label = { Text(stringResource(id = R.string.full_name_example_text)) },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                isError = isFullNameError.value,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(id = R.string.phone_number_text),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = phoneNumber.value,
                onValueChange = { phoneNumber.value = it },
                label = { Text(stringResource(id = R.string.phone_number_example_text)) },
                leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                isError = isPhoneNumberError.value,
                modifier = Modifier.fillMaxWidth()
            )
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

                    // Simple input validation
                    when {
                        profileImage.value == Uri.EMPTY -> {
                            isImageError.value = true
                            isLoading.value = false
                        }
                        email.value.isEmpty() -> {
                            isEmailError.value = true
                            emailErrorText.value = "Email is required"
                            isLoading.value = false
                        }
                        fullName.value.isEmpty() -> {
                            isFullNameError.value = true
                            isLoading.value = false
                        }
                        phoneNumber.value.isEmpty() -> {
                            isPhoneNumberError.value = true
                            isLoading.value = false
                        }
                        password.value.isEmpty() -> {
                            isPasswordError.value = true
                            passwordErrorText.value = "Password is required"
                            isLoading.value = false
                        }
                        else -> {
                            viewModel?.register(
                                fullName = fullName.value,
                                phoneNumber = phoneNumber.value,
                                profileImage = profileImage.value,
                                email = email.value,
                                password = password.value
                            )
                        }
                    }
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
                    Text(stringResource(id = R.string.register_text))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(
                onClick = { navController?.navigate(Screens.loginScreen) }
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(id = R.string.already_have_account))
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append(" " + stringResource(id = R.string.lets_login))
                        }
                    }
                )
            }
        }
    }

    registerFlow?.value.let {
        when (it) {
            is Resource.Failure -> {
                isLoading.value = false
                Log.d("Error", it.exception.message.toString())
                errorText.value = it.exception.message ?: "Registration failed"
                isError.value = true
            }
            is Resource.loading -> {
                // Do nothing, as isLoading is already set in onClick
            }
            is Resource.Success -> {
                isLoading.value = false
                LaunchedEffect(Unit) {
                    navController?.navigate(Screens.loginScreen) {
                        popUpTo(Screens.loginScreen) {
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

@Preview
@Composable
fun showRegisterScreen() {
    RegisterScreen(viewModel = null, navController = null)
}

@Composable
fun RegisterImage(
    selectedImageUri: MutableState<Uri?>,
    isError: MutableState<Boolean>
) {
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedImageUri.value = uri
        }
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selectedImageUri.value == null) {
            Image(
                painter = painterResource(id = R.drawable.pic),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(140.dp)
                    .border(
                        if (isError.value) BorderStroke(2.dp, Color.Red) else BorderStroke(0.dp, Color.Transparent)
                    )
                    .clip(RoundedCornerShape(70.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        singlePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
            )
        } else {
            AsyncImage(
                model = selectedImageUri.value,
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(70.dp))
                    .background(Color.LightGray)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        singlePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentScale = ContentScale.Crop
            )
        }
    }
}
