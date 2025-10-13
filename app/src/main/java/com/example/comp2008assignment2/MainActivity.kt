package com.example.comp2008assignment2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.comp2008assignment2.ui.theme.COMP2008Assignment2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            COMP2008Assignment2Theme {
                val navController = rememberNavController()
                val onlineViewModel: BookViewModel = viewModel()
                val localViewModel: LocalLibraryViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.Search, contentDescription = "Online Search") },
                                label = { Text("Online") },
                                selected = currentDestination?.hierarchy?.any { it.route == "online" } == true,
                                onClick = {
                                    navController.navigate("online") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.Home, contentDescription = "Local Library") },
                                label = { Text("Local") },
                                selected = currentDestination?.hierarchy?.any { it.route == "local" } == true,
                                onClick = {
                                    navController.navigate("local") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }) { innerPadding ->
                    NavHost(navController = navController, startDestination = "online", modifier = Modifier.padding(innerPadding)) {
                        composable("online") {
                            BookSearchScreen(
                                navController = navController,
                                viewModel = onlineViewModel
                            )
                        }
                        composable("local") {
                            LocalLibraryPage(
                                navController = navController,
                                viewModel = localViewModel
                            )
                        }
                        composable("add_book") {
                            AddBookScreen(
                                navController = navController,
                                viewModel = localViewModel
                            )
                        }
                        composable("detail/online") {
                            BookDetailScreen(
                                book = onlineViewModel.selectedBook,
                                isLocal = false,
                                descLoading = onlineViewModel.descLoading,
                                descError = onlineViewModel.descError,
                                onBack = { navController.navigateUp() },
                                onAction = { book -> localViewModel.saveBook(book!!) },
                                actionText = "Save to Local Library"
                            )
                        }
                        composable("detail/local") {
                            BookDetailScreen(
                                book = localViewModel.selectedBook,
                                isLocal = true,
                                descLoading = false,
                                descError = null,
                                onBack = { navController.navigateUp() },
                                onAction = { book -> localViewModel.deleteBook(book!!.key) },
                                actionText = "Delete from Local Library"
                            )
                        }
                    }
                }
            }
        }
    }
}