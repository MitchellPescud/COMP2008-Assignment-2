package com.example.comp2008assignment2

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

@Composable
fun ManualAddBookScreen(
    vm: BookViewModel,
    onBack: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf("") }
    var yearText by rememberSaveable { mutableStateOf("") }
    var coverUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var localCoverPath by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val year = yearText.toIntOrNull()

    // Permission launcher for camera and storage
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        val storageGranted = permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        if (cameraGranted || storageGranted) {
            // Permissions granted, proceed with action
        }
    }

    // Launcher for picking image from gallery
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coverUri = it
            localCoverPath = saveImageToInternalStorage(context, it)
        }
    }

    // Launcher for taking a photo
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val uri = saveBitmapToInternalStorage(context, it)
            coverUri = uri
            localCoverPath = uri?.path
        }
    }

    val canSave = title.isNotBlank() && author.isNotBlank() &&
            (yearText.isBlank() || (year != null && year in 0..2100))

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title and Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Book Manually",
                    style = MaterialTheme.typography.headlineSmall
                )
                Row {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.back_arrow),
                            contentDescription = "Back"
                        )
                    }
                    IconButton(
                        onClick = {
                            vm.addManualBook(title, author, year, localCoverPath) { onBack() }
                        },
                        enabled = canSave
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.check),
                            contentDescription = "Save"
                        )
                    }
                }
            }

            // Input Fields
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Author") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = yearText,
                onValueChange = { yearText = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text("Year (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (!canSave) {
                Text(
                    text = "Enter title & author. Year must be empty or a valid number (0–2100).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Cover Image Preview
            if (coverUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(coverUri),
                    contentDescription = "Selected cover",
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(8.dp)
                )
            }

            // Buttons for image selection and capture
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        )
                        pickImageLauncher.launch("image/*")
                    }
                ) {
                    Text("Pick Cover Image")
                }
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(android.Manifest.permission.CAMERA)
                        )
                        takePictureLauncher.launch(null)
                    }
                ) {
                    Text("Take Photo")
                }
            }

            Spacer(Modifier.weight(1f))
            Text(
                text = "Tip: This works fully offline. You can add or change a cover photo.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Helper function to save selected image to internal storage
private fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val fileName = "cover_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Helper function to save bitmap from camera to internal storage
private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val fileName = "cover_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }
        Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}