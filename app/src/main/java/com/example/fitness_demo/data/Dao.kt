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

    @Update
    suspend fun update(session: TrainingSession)

    @Delete
    suspend fun delete(session: TrainingSession)
}

@Dao
interface SetEntryDao {
    @Insert
    suspend fun insert(setEntry: SetEntry): Long

    @Update
    suspend fun update(setEntry: SetEntry)

    @Query("SELECT * FROM sets WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): SetEntry?

    @Query("SELECT * FROM sets WHERE sessionId = :sessionId ORDER BY setNumber ASC, id ASC")
    fun getForSession(sessionId: Int): Flow<List<SetEntry>>

    @Query("SELECT * FROM sets WHERE sessionId = :sessionId ORDER BY setNumber ASC, id ASC")
    suspend fun getForSessionOnce(sessionId: Int): List<SetEntry>

    @Query("DELETE FROM sets WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: Int)

    @Query("DELETE FROM sets WHERE id = :id")
    suspend fun deleteById(id: Int)

    // 将被删除组之后的所有组号减一，保证连续
    @Query("UPDATE sets SET setNumber = setNumber - 1 WHERE sessionId = :sessionId AND setNumber > :fromNumber")
    suspend fun decrementSetNumbersFrom(sessionId: Int, fromNumber: Int)

    @Query("UPDATE sets SET setNumber = setNumber + 1 WHERE sessionId = :sessionId AND setNumber >= :fromNumber")
    suspend fun incrementSetNumbersFrom(sessionId: Int, fromNumber: Int)

    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId ORDER BY id DESC LIMIT :limit")
    suspend fun getLastSetsForExercise(exerciseId: Int, limit: Int): List<SetEntry>
}

