package com.example.comp2008assignment2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.comp2008assignment2.ui.theme.COMP2008Assignment2Theme
import java.net.URLEncoder
import java.net.URLDecoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            COMP2008Assignment2Theme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "search",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("search") {
                            BookSearchScreen(
                                modifier = Modifier.padding(innerPadding),
                                onBookSelected = { book ->
                                    // Encode the full book key
                                    val encodedKey = URLEncoder.encode(book.key ?: "", "UTF-8")
                                    navController.navigate("bookDetail/$encodedKey")
                                }
                            )
                        }
                        composable("bookDetail/{bookKey}") { backStackEntry ->
                            val encodedKey = backStackEntry.arguments?.getString("bookKey") ?: ""
                            val bookKey = URLDecoder.decode(encodedKey, "UTF-8")
                            BookDetailScreen(
                                bookKey = bookKey, // Pass the full key (e.g., /works/OL17715454W)
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookSearchScreen(
    modifier: Modifier = Modifier,
    viewModel: BookViewModel = viewModel(),
    onBookSelected: (Book) -> Unit
) {
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
                        BookItem(book, onClick = { onBookSelected(book) })
                    }
                }
            }
        }
    }
}

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

@Composable
fun BookDetailScreen(bookKey: String, viewModel: BookViewModel = viewModel(), onBack: () -> Unit) {
    val bookDetail by viewModel.bookDetail.collectAsState()
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    LaunchedEffect(bookKey) {
        viewModel.fetchBookDetails(bookKey)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Back Button
        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            bookDetail != null -> {
                bookDetail?.let { book ->
                    // Book Cover
                    book.cover_i?.let { coverId ->
                        Image(
                            painter = rememberAsyncImagePainter("https://covers.openlibrary.org/b/id/$coverId-M.jpg"),
                            contentDescription = "Book cover",
                            modifier = Modifier
                                .size(300.dp)
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = 16.dp)
                        )
                    }
                    // Book Details
                    Text(
                        text = book.title ?: "Unknown Title",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Author(s): ${book.author_name?.joinToString(", ") ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Published: ${book.first_publish_year ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Description:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = book.description ?: "No description available",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookSearchScreenPreview() {
    COMP2008Assignment2Theme {
        BookSearchScreen(onBookSelected = {})
    }
}

@Preview(showBackground = true)
@Composable
fun BookDetailScreenPreview() {
    COMP2008Assignment2Theme {
        BookDetailScreen(bookKey = "/works/OL123W", onBack = {})
    }
}