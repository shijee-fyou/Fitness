package com.example.fitness_demo.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val exerciseDao: ExerciseDao,
    private val sessionDao: TrainingSessionDao,
    private val setDao: SetEntryDao
) {
    // Exercises
    fun observeExercises(): Flow<List<Exercise>> = exerciseDao.getAll()

    suspend fun ensureExerciseByName(name: String): Exercise {
        val existing = exerciseDao.getByName(name)
        if (existing != null) return existing
        val id = exerciseDao.insert(Exercise(name = name)).toInt()
        return exerciseDao.getById(id) ?: Exercise(id = id, name = name)
    }

    // Sessions
    fun observeSessions(): Flow<List<TrainingSession>> = sessionDao.getAll()

    suspend fun startNewSession(note: String? = null): TrainingSession {
        val id = sessionDao.insert(
            TrainingSession(startTimeMillis = System.currentTimeMillis(), note = note)
        ).toInt()
        return sessionDao.getById(id) ?: TrainingSession(id = id, startTimeMillis = System.currentTimeMillis(), note = note)
    }

    suspend fun getSessionById(id: Int): TrainingSession? = sessionDao.getById(id)

    // Sets
    fun observeSetsForSession(sessionId: Int): Flow<List<SetEntry>> = setDao.getForSession(sessionId)

    suspend fun addSet(
        sessionId: Int,
        exerciseId: Int,
        setNumber: Int,
        reps: Int,
        weightKg: Float?
    ): SetEntry {
        val id = setDao.insert(
            SetEntry(
                sessionId = sessionId,
                exerciseId = exerciseId,
                setNumber = setNumber,
                reps = reps,
                weightKg = weightKg
            )
        ).toInt()
        return SetEntry(
            id = id,
            sessionId = sessionId,
            exerciseId = exerciseId,
            setNumber = setNumber,
            reps = reps,
            weightKg = weightKg
        )
    }
}

