package com.example.comp2008assignment2

import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

@Composable
fun BookDetailScreen(
    viewModel: BookViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val book = viewModel.selectedBook ?: return
    var isInLibrary by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Check if book is in local library
    LaunchedEffect(book) {
        isInLibrary = viewModel.isBookInLibrary(book)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button
        Button(
            onClick = { navController.navigateUp() },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Book cover
        val coverUrl = book.localCoverPath ?: book.cover_i?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
        if (coverUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(coverUrl),
                contentDescription = "Book cover",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 16.dp)
            )
        }

        // Title and details
        Text(
            text = book.title ?: "Unknown Title",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Author(s): ${book.author_name?.joinToString(", ") ?: "Unknown"}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Published: ${book.first_publish_year ?: "Unknown"}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description section
        when {
            viewModel.descLoading -> {
                CircularProgressIndicator()
            }
            viewModel.descError != null -> {
                Text(
                    text = viewModel.descError ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
            else -> {
                Text(
                    text = "Description:",
                    style = TextStyle(
                        fontSize = 18.sp,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = book.description ?: "No description available.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add/Remove from Local Library Button
        Button(
            onClick = {
                coroutineScope.launch {
                    if (isInLibrary) {
                        viewModel.removeFromLocalLibrary(book)
                        isInLibrary = false
                    } else {
                        viewModel.addToLocalLibrary(book)
                        isInLibrary = true
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(if (isInLibrary) "Remove from Local Library" else "Add to Local Library")
        }

        Spacer(modifier = Modifier.height(32.dp)) // Extra bottom padding for readability
    }
}