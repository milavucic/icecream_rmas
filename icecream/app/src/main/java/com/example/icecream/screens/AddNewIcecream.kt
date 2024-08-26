package com.example.icecream.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.icecream.R
import com.example.icecream.repositories.Resource
import com.example.icecream.services.CameraService
import com.example.icecream.viewmodels.IcecreamViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.BuildConfig
import android.Manifest

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddNewIcecreamBottomSheet(
    icecreamViewModel: IcecreamViewModel?,
    location: MutableState<LatLng?>,
    sheetState: ModalBottomSheetState
) {
    val icecreamFlow = icecreamViewModel?.icecreamFlow?.collectAsState()
    val inputName = remember { mutableStateOf("") }
    val isNameError = remember { mutableStateOf(false) }
    val nameError = remember { mutableStateOf("Polje je obavezno") }
    val inputDescription = remember { mutableStateOf("") }
    val isDescriptionError = remember { mutableStateOf(false) }
    val descriptionError = remember { mutableStateOf("Polje je obavezno") }
    val selectedOption = remember { mutableStateOf(0) }
    val buttonIsEnabled = remember { mutableStateOf(true) }
    val buttonIsLoading = remember { mutableStateOf(false) }
    val selectedImage = remember { mutableStateOf<Uri?>(Uri.EMPTY) }
    val selectedGallery = remember { mutableStateOf<List<Uri>>(emptyList()) }
    val showedAlert = remember { mutableStateOf(false) }

    val launchCamera = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val cameraService = remember { CameraService(context) }
    val imageUri = remember { mutableStateOf<Uri?>(null) }

    val showImageSelectionDialogState = remember { mutableStateOf(false) }



    val launcherCamera = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                cameraService.imageUri?.let { uri ->
                    selectedGallery.value = selectedGallery.value + uri
                }
            }
        }
    )

    val launcherGallery = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            selectedGallery.value = selectedGallery.value + uris
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
            val storageGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
            if (cameraGranted || storageGranted) {
                if (cameraService.launchCameraRequested) {
                    cameraService.launchCamera(launcherCamera, "${BuildConfig.LIBRARY_PACKAGE_NAME}.fileprovider")
                } else {
                    cameraService.launchGallery(launcherGallery)
                }
            } else {
                Toast.makeText(context, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    if (showImageSelectionDialogState.value) {
        ShowImageSelectionDialog(
            cameraService = cameraService, // Pass cameraService here
            permissionLauncher = permissionLauncher,
            onDismiss = {
                showImageSelectionDialogState.value = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 50.dp, horizontal = 20.dp)
    ) {
        item {
            Text(
                text = stringResource(id = R.string.add_new_icecream_heading),
                style = MaterialTheme.typography.h5,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            Text(
                text = "Naziv štanda",
                style = MaterialTheme.typography.subtitle1
            )
        }
        item { Spacer(modifier = Modifier.height(5.dp)) }

        item {
            TextField(
                value = inputName.value,
                onValueChange = {
                    inputName.value = it
                    isNameError.value = it.isEmpty()
                },
                label = { Text("Unesite naziv štanda") },
                isError = isNameError.value,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true

            )
            if (isNameError.value) {
                Text(
                    text = nameError.value,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption
                )
            }
        }
        item {
            Text(
                text = "Opis",
                style = MaterialTheme.typography.subtitle1
            )
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item {
            TextField(
                value = inputDescription.value,
                onValueChange = {
                    inputDescription.value = it
                    isDescriptionError.value = it.isEmpty()
                },
                label = { Text("Unesite opis") },
                isError = isDescriptionError.value,
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )
            if (isDescriptionError.value) {
                Text(
                    text = descriptionError.value,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption
                )
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            Text(
                text = "Dodaj fotografije",
                style = MaterialTheme.typography.subtitle1
            )
        }
        item { Spacer(modifier = Modifier.height(5.dp)) }
        item {
            // Gallery selection (standard components)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                selectedGallery.value.forEach { uri ->
                    Image(
                        painter = rememberImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(4.dp)
                    )
                }
                IconButton(
                    onClick = {
                        // Handle gallery selection logic
                        showImageSelectionDialogState.value = true

                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item {
            Button(
                onClick = {
                    showedAlert.value = false
                    buttonIsLoading.value = true
                    icecreamViewModel?.saveIcecream(
                        name = inputName.value,
                        description = inputDescription.value,
                        galleryImages = selectedGallery.value,
                        location = location
                    )
                },
                enabled = buttonIsEnabled.value,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (buttonIsLoading.value) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Dodaj štand")
                }
            }
        }
        item { Spacer(modifier = Modifier.height(5.dp)) }
    }

    icecreamFlow?.value.let {
        when (it) {
            is Resource.Failure -> {
                Log.d("Stanje flowa", it.toString())
                buttonIsLoading.value = false
                val context = LocalContext.current
                if (!showedAlert.value) {
                    // Show a toast or alert
                    Toast.makeText(context, it.exception.message, Toast.LENGTH_LONG).show()
                    showedAlert.value = true
                    icecreamViewModel?.getAllIcecreams()
                }
            }
            is Resource.Loading -> {
                // Optionally handle loading state
            }
            is Resource.Success -> {
                Log.d("Stanje flowa", it.toString())
                buttonIsLoading.value = false
                val context = LocalContext.current
                if (!showedAlert.value) {
                    // Show a success toast or alert
                    Toast.makeText(context, "Uspesno dodato", Toast.LENGTH_LONG).show()
                    showedAlert.value = true
                    icecreamViewModel?.getAllIcecreams()
                }
            }
            null -> {}
        }
    }
}

@Composable
fun ShowImageSelectionDialog(
    cameraService: CameraService,
    permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Image Source") },
        text = { Text("Would you like to use the camera or select from the gallery?") },
        confirmButton = {
            TextButton(onClick = {
                permissionLauncher.launch(
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                )
                cameraService.launchCameraRequested = true
            }) {
                Text("Camera")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                permissionLauncher.launch(
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                )
                cameraService.launchCameraRequested = false
            }) {
                Text("Gallery")
            }
        }
    )
}


