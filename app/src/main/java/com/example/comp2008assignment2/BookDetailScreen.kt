package com.example.comp2008assignment2

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import android.content.res.Configuration

@Composable
fun BookDetailScreen(
    viewModel: BookViewModel,
    navController: NavHostController,
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val book = if (isOnline) viewModel.selectedOnlineBook else viewModel.selectedLocalBook
    val descLoading = if (isOnline) viewModel.onlineDescLoading else viewModel.localDescLoading
    val descError = if (isOnline) viewModel.onlineDescError else viewModel.localDescError
    var isInLibrary by remember { mutableStateOf(false) }
    var isFavourite by remember { mutableStateOf(book?.isFavourite ?: false) }
    var showDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.smallestScreenWidthDp >= 600
    val isSplitScreen = isTablet && isLandscape

    // Check if book is in local library and favourite status
    LaunchedEffect(book) {
        if (book != null) {
            isInLibrary = viewModel.isBookInLibrary(book)
            isFavourite = book.isFavourite
        }
    }

    if (book == null) {
        Text(
            text = "No book selected",
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
            style = MaterialTheme.typography.bodyLarge
        )
        return
    }

    // Confirmation Dialog for Deletion
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to remove this book from your local library?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        viewModel.removeFromLocalLibrary(book) { navController.navigateUp() }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button (only shown if not in split-screen mode)
        if (!isSplitScreen) {
            Button(
                onClick = { navController.navigateUp() },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text("Back")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        else {
            Spacer(modifier = Modifier.height(20.dp))
        }

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

        // Add/Remove from Local Library Button
        Button(
            onClick = {
                if (isInLibrary) {
                    showDialog = true
                } else {
                    coroutineScope.launch {
                        viewModel.addToLocalLibrary(book, isFavourite)
                        isInLibrary = true
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(if (isInLibrary) "Remove from Local Library" else "Add to Local Library")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Favourite/Unfavourite Button
        Button(
            onClick = {
                coroutineScope.launch {
                    if (isOnline && !isInLibrary) {
                        viewModel.addToLocalLibrary(book, true)
                        isInLibrary = true
                        isFavourite = true
                    } else {
                        viewModel.toggleFavourite(book)
                        isFavourite = !isFavourite
                        // Navigate back if unfavourited from favourites screen
                        if (!isFavourite && currentRoute == "local_detail" && navController.previousBackStackEntry?.destination?.route == "favourites") {
                            navController.navigateUp()
                        }
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(if (isFavourite) "Remove from Favourites" else "Add to Favourites")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}