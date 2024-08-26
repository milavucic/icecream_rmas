package com.example.icecream.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import java.io.File

class CameraService(private val context: Context) {
    var imageUri: Uri? = null
    var launchCameraRequested: Boolean = false

    fun launchCamera(
        takePictureLauncher: ActivityResultLauncher<Uri>,
        fileProviderAuthority: String
    ) {
        val photoFile: File? = createImageFile()
        photoFile?.let {
            imageUri = FileProvider.getUriForFile(
                context,
                fileProviderAuthority,
                it
            )
            takePictureLauncher.launch(imageUri)
        }
    }

    fun launchGallery(pickImageLauncher: ActivityResultLauncher<String>) {
        pickImageLauncher.launch("image/*")
    }

    private fun createImageFile(): File? {
        val storageDir: File? = context.getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        )
    }
}
