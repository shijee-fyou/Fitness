package com.example.fitness_demo.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val exerciseDao: ExerciseDao,
    private val sessionDao: TrainingSessionDao,
    private val setDao: SetEntryDao
) {
    // Exercises
    fun observeExercises(): Flow<List<Exercise>> = exerciseDao.getAll()
    fun observeExercisesByMuscle(group: String): Flow<List<Exercise>> = exerciseDao.getByMuscleGroup(group)

    suspend fun ensureExerciseByName(name: String, muscleGroup: String = "Other", description: String? = null): Exercise {
        val existing = exerciseDao.getByName(name)
        if (existing != null) return existing
        val id = exerciseDao.insert(Exercise(name = name, muscleGroup = muscleGroup, description = description)).toInt()
        return exerciseDao.getById(id) ?: Exercise(id = id, name = name, muscleGroup = muscleGroup, description = description)
    }
    suspend fun getExerciseById(id: Int): Exercise? = exerciseDao.getById(id)

    // Sessions
    fun observeSessions(): Flow<List<TrainingSession>> = sessionDao.getAll()

    suspend fun startNewSession(note: String? = null): TrainingSession {
        val id = sessionDao.insert(
            TrainingSession(startTimeMillis = System.currentTimeMillis(), note = note)
        ).toInt()
        return sessionDao.getById(id) ?: TrainingSession(id = id, startTimeMillis = System.currentTimeMillis(), note = note)
    }

    suspend fun getSessionById(id: Int): TrainingSession? = sessionDao.getById(id)
    suspend fun deleteSessionById(id: Int) {
        val s = sessionDao.getById(id) ?: return
        sessionDao.delete(s)
        // SetEntry rows are deleted via CASCADE
    }
    suspend fun recreateSession(session: TrainingSession): TrainingSession {
        val newId = sessionDao.insert(
            TrainingSession(startTimeMillis = session.startTimeMillis, note = session.note)
        ).toInt()
        return sessionDao.getById(newId) ?: TrainingSession(id = newId, startTimeMillis = session.startTimeMillis, note = session.note)
    }

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

    suspend fun deleteSet(id: Int) {
        setDao.deleteById(id)
    }

    suspend fun seedIfEmpty(defaults: List<Exercise>) {
        if (exerciseDao.countAll() > 0) return
        defaults.forEach { ex -> exerciseDao.insert(ex) }
    }

    suspend fun seedDefaultsEnsure(defaults: List<Exercise>) {
        defaults.forEach { ex -> ensureExerciseByName(ex.name, ex.muscleGroup, ex.description) }
    }
}

