package com.example.comp2008assignment2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.comp2008assignment2.ui.theme.COMP2008Assignment2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            COMP2008Assignment2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BookSearchScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun BookSearchScreen(modifier: Modifier = Modifier, viewModel: BookViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf(SearchType.GENERAL) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Type Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { searchType = SearchType.GENERAL },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (searchType == SearchType.GENERAL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("General")
            }
            Button(
                onClick = { searchType = SearchType.TITLE },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (searchType == SearchType.TITLE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Title")
            }
            Button(
                onClick = { searchType = SearchType.AUTHOR },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (searchType == SearchType.AUTHOR) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Author")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by title or author") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            trailingIcon = {
                Button(
                    onClick = { viewModel.searchBooks(searchQuery, searchType) },
                    enabled = searchQuery.isNotBlank()
                ) {
                    Text("Search")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Loading, Error, or Book List
        when {
            viewModel.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            viewModel.errorMessage != null -> {
                Text(
                    text = viewModel.errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            viewModel.books.isEmpty() -> {
                Text(
                    text = "Welcome! - Try Searching for a Book",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            else -> {
                val configuration = LocalConfiguration.current
                val isTablet = configuration.smallestScreenWidthDp >= 600
                val isLandscape =
                    configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE


                val columns = when {
                    isTablet && isLandscape -> 3
                    isTablet && !isLandscape -> 2
                    !isTablet && isLandscape -> 2
                    else -> 1
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(viewModel.books) { book ->
                        BookItem(book)
                    }
                }
            }
        }
    }
}

@Composable
fun BookItem(book: Book) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book Cover (if available)
            book.cover_i?.let { coverId ->
                Image(
                    painter = rememberAsyncImagePainter("https://covers.openlibrary.org/b/id/$coverId-S.jpg"),
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

@Preview(showBackground = true)
@Composable
fun BookSearchScreenPreview() {
    COMP2008Assignment2Theme {
        BookSearchScreen()
    }
}