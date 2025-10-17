package com.example.comp2008assignment2

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.util.Locale
import android.content.res.Configuration

@Composable
fun BookSearchScreen(
    navController: NavHostController,
    viewModel: BookViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf(SearchType.GENERAL) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLandscape) {
            // Landscape: Title and radio buttons in a Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Online Search",
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(
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
                                text = type.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) },
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Portrait: Title and radio buttons in Column
            Text(
                text = "Online Search",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
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
                            text = type.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) },
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
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
                val isTablet = configuration.smallestScreenWidthDp >= 600
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