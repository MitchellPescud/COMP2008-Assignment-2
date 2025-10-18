package com.example.comp2008assignment2

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch

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
    var showDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Check if book is in local library
    LaunchedEffect(book) {
        if (book != null) {
            isInLibrary = viewModel.isBookInLibrary(book)
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

    // Confirmation Dialog
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
        val snackbars = remember { SnackbarHostState() }
        LaunchedEffect(Unit) { /* keeps host alive */ }
        Spacer(Modifier.height(8.dp))
        SnackbarHost(hostState = snackbars)

        if (!isInLibrary) {
            // Two separate add actions when not saved yet
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1) Add to Favourites (for now, same store as local; you can split later)
                Button(onClick = {
                    coroutineScope.launch {
                        viewModel.addToFavorites(book)
                        isInLibrary = true
                        snackbars.showSnackbar("Added to Favourites")
                    }
                }) {
                    Text("Add to Favourites")
                }

                // 2) Add to Local Library
                OutlinedButton(onClick = {
                    coroutineScope.launch {
                        viewModel.addToLocalLibrary(book)
                        isInLibrary = true
                        snackbars.showSnackbar("Added to Local Library")
                    }
                }) {
                    Text("Add to Local Library")
                }
            }
        } else {
            // Already saved → show Remove action
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Remove from Local Library")
            }
        }

        Spacer(modifier = Modifier.height(32.dp)) // Extra bottom padding for readability
    }
}