package com.example.fitness_demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fitness_demo.ui.theme.Fitness_DemoTheme
import com.example.fitness_demo.ui.FitnessAppRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Fitness_DemoTheme {
                val app = application as FitnessApp
                FitnessAppRoot(repository = app.container.repository)
            }
        }
    }
}