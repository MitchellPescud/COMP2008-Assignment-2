package com.example.comp2008assignment2

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun BookItem(book: Book, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book Cover (if available)
            val coverUrl = book.localCoverPath ?: book.cover_i?.let { "https://covers.openlibrary.org/b/id/$it-S.jpg" }
            if (coverUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(coverUrl),
                    contentDescription = "Book cover",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 16.dp)
                )
            }
            // Book Details
            Column {
                Text(
                    text = book.title ?: "Unknown Title",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Author(s): ${book.author_name?.joinToString(", ") ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Published: ${book.first_publish_year ?: "Unknown"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}