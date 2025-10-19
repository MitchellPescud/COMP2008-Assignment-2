package com.example.comp2008assignment2

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.core.content.ContextCompat
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
    var description by rememberSaveable { mutableStateOf("") }
    var coverUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var localCoverPath by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val year = yearText.toIntOrNull()
    val scrollState = rememberScrollState()

    // Initialize CAMERA permission state
    var isCameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
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

    // Permission launcher for camera and storage
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        isCameraPermissionGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        if (isCameraPermissionGranted) {
            // Launch camera immediately after permission is granted
            takePictureLauncher.launch(null)
        } else if (permissions[android.Manifest.permission.CAMERA] == false) {
            Toast.makeText(context, "Camera permission is required to take a photo.", Toast.LENGTH_LONG).show()
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

    val canSave = title.isNotBlank() && author.isNotBlank() &&
            (yearText.isBlank() || (year != null && year in 0..2100))

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
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
                            vm.addManualBook(title, author, year, localCoverPath, description) { onBack() }
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

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 10
            )

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
                        if (isCameraPermissionGranted) {
                            takePictureLauncher.launch(null)
                        } else {
                            permissionLauncher.launch(
                                arrayOf(android.Manifest.permission.CAMERA)
                            )
                        }
                    }
                ) {
                    Text("Take Photo")
                }
            }

            Spacer(Modifier.weight(1f))
            Text(
                text = "Tip: This works fully offline. You can add or change a cover photo and description.",
                style = MaterialTheme.typography.bodySmall
            )
            // Additional spacer to ensure content is fully scrollable
            Spacer(Modifier.height(16.dp))
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