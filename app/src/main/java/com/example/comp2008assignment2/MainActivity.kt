package com.example.comp2008assignment2

import android.app.Application
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import coil.compose.rememberAsyncImagePainter
import com.example.comp2008assignment2.ui.theme.COMP2008Assignment2Theme
import java.util.Locale
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        lateinit var bookDao: BookDao
        lateinit var app: Application
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application
        val db = Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "books.db"
        ).build()
        bookDao = db.bookDao()

        enableEdgeToEdge()
        setContent {
            COMP2008Assignment2Theme {
                val navController = rememberNavController()
                val viewModel: BookViewModel = viewModel()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(navController = navController, startDestination = "search") {
                        composable("search") {
                            BookSearchScreen(
                                navController = navController,
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        composable("local") {
                            LocalLibraryScreen(
                                navController = navController,
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        composable("detail") {
                            BookDetailScreen(
                                viewModel = viewModel,
                                navController = navController,
                                modifier = Modifier.padding(innerPadding)
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
    navController: NavHostController,
    viewModel: BookViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf(SearchType.GENERAL) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Online Search", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { navController.navigate("local") }) {
                Text("My Local Library")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Books") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            trailingIcon = {
                Button(
                    onClick = { viewModel.searchBooks(searchQuery, searchType) },
                    enabled = searchQuery.isNotBlank(),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Search")
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Search Type Radio Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(SearchType.GENERAL, SearchType.TITLE, SearchType.AUTHOR).forEach { type ->
                Row(
                    Modifier
                        .clickable { searchType = type }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (searchType == type),
                        onClick = { searchType = type }
                    )
                    Text(
                        text = type.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

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
                        BookItem(book = book, onClick = {
                            viewModel.selectBook(book)
                            navController.navigate("detail")
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun LocalLibraryScreen(
    navController: NavHostController,
    viewModel: BookViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadLocalBooks("")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Local Library", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { navController.navigate("search") }) {
                Text("Back to Online Search")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar for Local
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Local Books") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            trailingIcon = {
                Button(
                    onClick = { viewModel.loadLocalBooks(searchQuery) },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Search")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Loading, Error, or Book List
        when {
            viewModel.localIsLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            viewModel.localErrorMessage != null -> {
                Text(
                    text = viewModel.localErrorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            viewModel.localBooks.isEmpty() -> {
                Text(
                    text = "No books in your local library yet.",
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
                    items(viewModel.localBooks) { book ->
                        BookItem(book = book, onClick = {
                            viewModel.selectBook(book)
                            navController.navigate("detail")
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun BookDetailScreen(viewModel: BookViewModel, navController: NavHostController, modifier: Modifier = Modifier) {
    val book = viewModel.selectedBook ?: return
    val scrollState = rememberScrollState()
    var isInLibrary by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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

@Preview(showBackground = true)
@Composable
fun BookSearchScreenPreview() {
    COMP2008Assignment2Theme {
        val navController = rememberNavController()
        val viewModel: BookViewModel = viewModel()
        BookSearchScreen(navController = navController, viewModel = viewModel)
    }
}