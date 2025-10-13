package com.example.comp2008assignment2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun LocalLibraryPage(
    navController: NavHostController,
    viewModel: LocalLibraryViewModel = viewModel()
) {
    var searchType by remember { mutableStateOf(SearchType.GENERAL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 🔍 Search field
        TextField(
            value = viewModel.searchQuery,
            onValueChange = { query: String -> viewModel.updateSearchQuery(query, searchType) },
            label = {
                Text(
                    if (searchType == SearchType.GENERAL)
                        "Search"
                    else
                        "Search by ${searchType.name.lowercase().replaceFirstChar { it.uppercase() }}"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        // 🔘 Search type radio options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val options = listOf(
                SearchType.GENERAL to "All",
                SearchType.TITLE to "Title",
                SearchType.AUTHOR to "Author"
            )

            options.forEach { (type, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    RadioButton(
                        selected = searchType == type,
                        onClick = {
                            searchType = type
                            viewModel.updateSearchQuery(viewModel.searchQuery, searchType)
                        }
                    )
                    Text(label)
                }
            }
        }

        val books by viewModel.filteredBooks.collectAsState()

        if (books.isEmpty()) {
            Text(
                text = "Your local library is empty or no results found.",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            val configuration = LocalConfiguration.current
            val isTablet = configuration.smallestScreenWidthDp >= 600
            val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

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
                items(books) { book ->
                    BookItem(book = book, onClick = {
                        viewModel.selectBook(book)
                        navController.navigate("detail/local")
                    })
                }
            }
        }
    }
}
