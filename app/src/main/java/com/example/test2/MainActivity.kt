package com.example.test2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.test2.presentation.habits.HabitScreen
import com.example.test2.presentation.habits.NotesScreen
import com.example.test2.ui.theme.Test2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Test2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }
}

/**
 * 应用主界面组合函数
 */
@Composable
fun MainApp() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "notes") {
        composable("habits") {
            HabitScreen(
                onNavigateToNotes = {
                    navController.navigate("notes")
                },
                onNavigateToHabitNotes = { habitId ->
                    navController.navigate("notes/$habitId")
                }
            )
        }
        
        composable("notes") {
            NotesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "notes/{habitId}",
            arguments = listOf(
                navArgument("habitId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            NotesScreen(
                habitId = habitId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}