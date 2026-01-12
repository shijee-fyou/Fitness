package com.example.fitness_demo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.fitness_demo.data.AppRepository
import com.example.fitness_demo.ui.screens.HistoryScreen
import com.example.fitness_demo.ui.screens.HomeScreen
import com.example.fitness_demo.ui.screens.ExercisesScreen
import com.example.fitness_demo.ui.screens.SettingsScreen
import com.example.fitness_demo.ui.screens.SessionDetailScreen
import com.example.fitness_demo.ui.screens.StartSessionScreen
import com.example.fitness_demo.ui.screens.ExerciseDetailScreen

@Composable
fun FitnessNavGraph(
    repository: AppRepository,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.HOME,
        modifier = modifier
    ) {
        composable(Destinations.HOME) {
            HomeScreen(
                onStartSessionClick = { navController.navigate(Destinations.START_SESSION) },
                onHistoryClick = { navController.navigate(Destinations.HISTORY) }
            )
        }
        composable(Destinations.START_SESSION) {
            StartSessionScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onSessionStarted = { sessionId ->
                    navController.navigate("${Destinations.SESSION_DETAIL}/$sessionId") {
                        popUpTo(Destinations.HOME)
                    }
                }
            )
        }
        composable("${Destinations.SESSION_DETAIL}/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId")?.toIntOrNull()
            if (sessionId != null) {
                SessionDetailScreen(
                    sessionId = sessionId,
                    repository = repository,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(Destinations.HISTORY) {
            HistoryScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onOpenSession = { sessionId ->
                    navController.navigate("${Destinations.SESSION_DETAIL}/$sessionId")
                }
            )
        }
        composable(Destinations.EXERCISES) {
            ExercisesScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onOpenExercise = { exId ->
                    navController.navigate("${Destinations.EXERCISE_DETAIL}/$exId")
                }
            )
        }
        composable(Destinations.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable("${Destinations.EXERCISE_DETAIL}/{exerciseId}") { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId")?.toIntOrNull()
            if (exerciseId != null) {
                ExerciseDetailScreen(
                    exerciseId = exerciseId,
                    repository = repository,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

