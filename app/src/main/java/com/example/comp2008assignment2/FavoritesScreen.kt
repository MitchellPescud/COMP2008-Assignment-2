package com.example.comp2008assignment2

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.CircularProgressIndicator
import coil.compose.AsyncImage

@Composable
fun FavoritesScreen(
    navController: NavHostController,
    viewModel: BookViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { viewModel.loadLocalBooks() }

    val localBooks = viewModel.localBooks
    val localLoading = viewModel.localIsLoading
    val localError = viewModel.localErrorMessage

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Favourite Books",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        when {
            localLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            localError != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = localError ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            localBooks.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No favourite books yet.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(localBooks) { book ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val coverModel =
                                    book.localCoverPath
                                        ?: book.cover_i?.let { "https://covers.openlibrary.org/b/id/${it}-S.jpg" }

                                AsyncImage(
                                    model = coverModel,
                                    contentDescription = "Book cover for ${book.title}",
                                    modifier = Modifier.size(64.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = book.title ?: "Unknown Title",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = book.author_name?.joinToString(", ") ?: "Unknown author",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Year: ${book.first_publish_year ?: "-"}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                IconButton(onClick = { viewModel.removeFromLocalLibrary(book) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}