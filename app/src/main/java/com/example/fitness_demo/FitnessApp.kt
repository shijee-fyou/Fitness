package com.example.fitness_demo

import android.app.Application
import com.example.fitness_demo.di.AppContainer

class FitnessApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

