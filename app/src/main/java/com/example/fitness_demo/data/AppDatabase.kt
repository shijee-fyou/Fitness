package com.example.fitness_demo.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Exercise::class,
        TrainingSession::class,
        SetEntry::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun trainingSessionDao(): TrainingSessionDao
    abstract fun setEntryDao(): SetEntryDao
}

