package com.example.comp2008assignment2

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookScreen(
    navController: NavController,
    viewModel: LocalLibraryViewModel
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Book") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Author") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUri),
                    contentDescription = "Book Cover",
                    modifier = Modifier
                        .size(120.dp, 180.dp)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.placeholder),
                    contentDescription = "Book Cover Placeholder",
                    modifier = Modifier
                        .size(120.dp, 180.dp)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Button(onClick = { launcher.launch("image/*") }) {
                Text("Select Cover Image")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val newBook = LocalBook(
                    key = UUID.randomUUID().toString(),
                    title = title,
                    author_name = author,
                    first_publish_year = null,
                    cover_i = null,
                    description = null,
                    cover_image_path = imageUri?.toString()
                )
                viewModel.addBook(newBook)
                navController.popBackStack()
            }) {
                Text("Add Book")
            }
        }
    }
}