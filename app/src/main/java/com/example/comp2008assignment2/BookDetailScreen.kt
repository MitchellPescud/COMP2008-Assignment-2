package com.example.comp2008assignment2

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import java.io.File

@Composable
fun BookDetailScreen(
    book: DisplayBook?,
    isLocal: Boolean,
    descLoading: Boolean,
    descError: String?,
    onBack: () -> Unit,
    onAction: (DisplayBook?) -> Unit,
    actionText: String,
    modifier: Modifier = Modifier
) {
    if (book == null) return

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        book.coverId?.let { coverId ->
            val painter = if (isLocal && book.coverPath != null) {
                rememberAsyncImagePainter(File(book.coverPath))
            } else {
                rememberAsyncImagePainter("https://covers.openlibrary.org/b/id/$coverId-M.jpg")
            }
            Image(
                painter = painter,
                contentDescription = "Book cover",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 16.dp)
            )
        }

        Text(
            text = book.title ?: "Unknown Title",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Author(s): ${book.authors ?: "Unknown"}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Published: ${book.publishYear ?: "Unknown"}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            descLoading -> {
                CircularProgressIndicator()
            }
            descError != null -> {
                Text(
                    text = descError,
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

        Button(onClick = { onAction(book) }) {
            Text(actionText)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}