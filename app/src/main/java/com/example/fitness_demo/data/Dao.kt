package com.example.fitness_demo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exercise: Exercise): Long

    @Query("SELECT * FROM exercises ORDER BY name")
    fun getAll(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE muscleGroup = :group ORDER BY name")
    fun getByMuscleGroup(group: String): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Exercise?

    @Query("SELECT * FROM exercises WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Exercise?

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun countAll(): Int

    @Delete
    suspend fun delete(exercise: Exercise)
}

@Dao
interface TrainingSessionDao {
    @Insert
    suspend fun insert(session: TrainingSession): Long

    @Query("SELECT * FROM sessions ORDER BY startTimeMillis DESC")
    fun getAll(): Flow<List<TrainingSession>>

    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): TrainingSession?

    @Delete
    suspend fun delete(session: TrainingSession)
}

@Dao
interface SetEntryDao {
    @Insert
    suspend fun insert(setEntry: SetEntry): Long

    @Update
    suspend fun update(setEntry: SetEntry)

    @Query("SELECT * FROM sets WHERE sessionId = :sessionId ORDER BY setNumber ASC, id ASC")
    fun getForSession(sessionId: Int): Flow<List<SetEntry>>

    @Query("DELETE FROM sets WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: Int)

    @Query("DELETE FROM sets WHERE id = :id")
    suspend fun deleteById(id: Int)
}

