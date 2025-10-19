package com.example.comp2008assignment2

import android.app.Application
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.comp2008assignment2.ui.theme.COMP2008Assignment2Theme

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
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                if (isLandscape) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Row {
                            NavigationRailBar(navController)
                            NavHost(
                                navController = navController,
                                startDestination = "search",
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
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
                                composable("online_detail") {
                                    BookDetailScreen(
                                        viewModel = viewModel,
                                        navController = navController,
                                        isOnline = true
                                    )
                                }
                                composable("local_detail") {
                                    BookDetailScreen(
                                        viewModel = viewModel,
                                        navController = navController,
                                        isOnline = false
                                    )
                                }
                                composable("favourites") {
                                    FavouritesScreen(
                                        navController = navController,
                                        viewModel = viewModel
                                    )
                                }
                                composable("manual_add") {
                                    ManualAddBookScreen(
                                        vm = viewModel,
                                        onBack = { navController.navigateUp() }
                                    )
                                }
                            }
                        }
                    }
                } else {
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
                            composable("online_detail") {
                                BookDetailScreen(
                                    viewModel = viewModel,
                                    navController = navController,
                                    isOnline = true
                                )
                            }
                            composable("local_detail") {
                                BookDetailScreen(
                                    viewModel = viewModel,
                                    navController = navController,
                                    isOnline = false
                                )
                            }
                            composable("favourites") {
                                FavouritesScreen(
                                    navController = navController,
                                    viewModel = viewModel
                                )
                            }
                            composable("manual_add") {
                                ManualAddBookScreen(
                                    vm = viewModel,
                                    onBack = { navController.navigateUp() }
                                )
                            }
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
        NavigationItem("Favourites", "favourites", R.drawable.favourite)
    )

    // Map child routes to their parent routes for highlighting
    val routeToParent = mapOf(
        "online_detail" to "search",
        "local_detail" to "local",
        "manual_add" to "local"
    )

    NavigationBar {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        // Determine the effective route for highlighting (parent or current)
        val effectiveRoute = routeToParent[currentRoute] ?: currentRoute

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(ImageVector.vectorResource(id = item.icon), contentDescription = item.title) },
                label = { Text(item.title) },
                selected = effectiveRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun NavigationRailBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem("Search", "search", R.drawable.search),
        NavigationItem("Local Library", "local", R.drawable.library),
        NavigationItem("Favorites", "favorites", R.drawable.favourite)
    )

    // Map child routes to their parent routes for highlighting
    val routeToParent = mapOf(
        "online_detail" to "search",
        "local_detail" to "local",
        "manual_add" to "local"
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    // Determine the effective route for highlighting (parent or current)
    val effectiveRoute = routeToParent[currentRoute] ?: currentRoute

    NavigationRail(
        modifier = Modifier
            .fillMaxHeight()
            .width(100.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            items.forEach { item ->
                NavigationRailItem(
                    icon = {
                        Icon(
                            ImageVector.vectorResource(id = item.icon),
                            contentDescription = item.title
                        )
                    },
                    label = { Text(item.title) },
                    selected = effectiveRoute == item.route,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

data class NavigationItem(val title: String, val route: String, val icon: Int)