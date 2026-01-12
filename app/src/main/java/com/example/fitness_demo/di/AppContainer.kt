package com.example.fitness_demo.di

import android.content.Context
import androidx.room.Room
import com.example.fitness_demo.data.AppDatabase
import com.example.fitness_demo.data.AppRepository
import com.example.fitness_demo.data.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "fitness.db"
    ).fallbackToDestructiveMigration().build()

    val repository: AppRepository = AppRepository(
        exerciseDao = database.exerciseDao(),
        sessionDao = database.trainingSessionDao(),
        setDao = database.setEntryDao()
    )

    private val ioScope = CoroutineScope(Dispatchers.IO)

    init {
        ioScope.launch {
            repository.seedDefaultsEnsure(SeedData.defaultExercises)
        }
    }
}

