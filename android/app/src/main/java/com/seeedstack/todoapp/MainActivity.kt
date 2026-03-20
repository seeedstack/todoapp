package com.seeedstack.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.seeedstack.todoapp.data.local.TokenStore
import com.seeedstack.todoapp.ui.theme.TodoAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenStore: TokenStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoAppTheme {
                val navController = rememberNavController()
                val startDest = if (tokenStore.hasToken()) "main" else "login"

                NavHost(navController, startDestination = startDest) {
                    composable("login") {
                        com.seeedstack.todoapp.ui.login.LoginScreen(
                            onLoginSuccess = {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("main") {
                        MainScaffold(
                            isAdmin = tokenStore.isAdmin(),
                            onLogout = {
                                tokenStore.clear()
                                navController.navigate("login") {
                                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScaffold(isAdmin: Boolean, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = androidx.compose.ui.graphics.Color(0xFF1A1D27)) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Task, "Tasks") },
                    label = { Text("Tasks") },
                    selected = currentRoute == "tasks",
                    onClick = { navController.navigate("tasks") { launchSingleTop = true } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, "Logs") },
                    label = { Text("Logs") },
                    selected = currentRoute == "logs",
                    onClick = { navController.navigate("logs") { launchSingleTop = true } }
                )
                if (isAdmin) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.AdminPanelSettings, "Admin") },
                        label = { Text("Admin") },
                        selected = currentRoute == "admin",
                        onClick = { navController.navigate("admin") { launchSingleTop = true } }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = "tasks", modifier = Modifier.padding(padding)) {
            composable("tasks") {
                com.seeedstack.todoapp.ui.tasks.TaskListScreen(onLogout = onLogout)
            }
            composable("logs") {
                com.seeedstack.todoapp.ui.logs.LogScreen(onLogout = onLogout)
            }
            if (isAdmin) composable("admin") { Surface { Text("Admin placeholder") } }
        }
    }
}
