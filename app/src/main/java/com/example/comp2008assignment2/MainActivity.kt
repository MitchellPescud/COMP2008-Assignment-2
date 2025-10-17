package com.example.comp2008assignment2

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.comp2008assignment2.ui.theme.COMP2008Assignment2Theme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.fillMaxSize

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
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavigationBar(navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "search",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("search") {
                            BookSearchScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("local") {
                            LocalLibraryScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("detail") {
                            BookDetailScreen(
                                viewModel = viewModel,
                                navController = navController
                            )
                        }
                        composable("favorites") {
                            FavoritesScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem("Search", "search", R.drawable.search),
        NavigationItem("Local Library", "local", R.drawable.library),
        NavigationItem("Favorites", "favorites", R.drawable.favourite)
    )
    NavigationBar {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(ImageVector.vectorResource(id = item.icon), contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class NavigationItem(val title: String, val route: String, val icon: Int)