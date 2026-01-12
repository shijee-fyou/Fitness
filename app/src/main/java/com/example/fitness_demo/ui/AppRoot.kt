package com.example.fitness_demo.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitness_demo.data.AppRepository
import com.example.fitness_demo.ui.navigation.Destinations
import com.example.fitness_demo.ui.navigation.FitnessNavGraph

data class BottomItem(val route: String, val label: String, val icon: @Composable () -> Unit)

@Composable
fun FitnessAppRoot(
    repository: AppRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val items = listOf(
        BottomItem(Destinations.HOME, "训练", { Icon(Icons.Default.FitnessCenter, contentDescription = null) }),
        BottomItem(Destinations.HISTORY, "记录", { Icon(Icons.Default.History, contentDescription = null) }),
        BottomItem(Destinations.EXERCISES, "练习", { Icon(Icons.Default.List, contentDescription = null) }),
        BottomItem(Destinations.SETTINGS, "设置", { Icon(Icons.Default.Settings, contentDescription = null) })
    )

    androidx.compose.material3.Scaffold(
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = item.icon,
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        FitnessNavGraph(
            repository = repository,
            navController = navController,
            modifier = Modifier.then(Modifier).padding(innerPadding)
        )
    }
}

