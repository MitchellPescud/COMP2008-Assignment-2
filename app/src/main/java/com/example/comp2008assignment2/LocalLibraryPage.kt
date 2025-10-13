package com.example.comp2008assignment2

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import java.io.File

@Composable
fun LocalLibraryPage(viewModel: LocalLibraryViewModel = viewModel()) {
    val localBooks by viewModel.localBooks.collectAsState()

    if (localBooks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Your local library is empty.")
        }
    } else {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(localBooks) { book ->
                LocalBookItem(book = book, onDelete = { viewModel.deleteBook(book.key) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun LocalBookItem(book: LocalBook, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(8.dp)) {
            book.cover_image_path?.let {
                Image(
                    painter = rememberAsyncImagePainter(File(it)),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp, 150.dp),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(modifier = Modifier.size(100.dp, 150.dp), contentAlignment = Alignment.Center) {
                Text("No Image")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = book.title ?: "No Title")
                Text(text = book.author_name ?: "No Author")
                Text(text = book.first_publish_year?.toString() ?: "N/A")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}
