package com.example.fitness_demo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitness_demo.data.AppRepository
import com.example.fitness_demo.data.Exercise
import com.example.fitness_demo.data.SetEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class SessionViewModel(
    private val repository: AppRepository,
    private val sessionId: Int
) : ViewModel() {

    val exercises: StateFlow<List<Exercise>> =
        repository.observeExercises().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val sets: StateFlow<List<SetEntry>> =
        repository.observeSetsForSession(sessionId).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedExerciseId = MutableStateFlow<Int?>(null)
    val selectedExerciseId: StateFlow<Int?> = _selectedExerciseId.asStateFlow()

    private val _repsText = MutableStateFlow("")
    val repsText: StateFlow<String> = _repsText.asStateFlow()

    private val _weightText = MutableStateFlow("")
    val weightText: StateFlow<String> = _weightText.asStateFlow()

    private val _restSeconds = MutableStateFlow<Int?>(null)
    val restSeconds: StateFlow<Int?> = _restSeconds.asStateFlow()

    private val _restTotalSeconds = MutableStateFlow<Int?>(null)
    val restTotalSeconds: StateFlow<Int?> = _restTotalSeconds.asStateFlow()

    fun selectExercise(id: Int?) { _selectedExerciseId.value = id }
    fun setRepsText(t: String) { _repsText.value = t }
    fun setWeightText(t: String) { _weightText.value = t }

    fun addSet() {
        val exId = _selectedExerciseId.value ?: return
        val reps = _repsText.value.toIntOrNull() ?: return
        val weight = _weightText.value.toFloatOrNull()
        val nextSetNum = (sets.value.maxOfOrNull { it.setNumber } ?: 0) + 1
        viewModelScope.launch {
            repository.addSet(
                sessionId = sessionId,
                exerciseId = exId,
                setNumber = nextSetNum,
                reps = reps,
                weightKg = weight
            )
            _repsText.value = ""
            _weightText.value = ""
            startRest(90)
        }
    }

    fun deleteSet(id: Int) {
        viewModelScope.launch {
            repository.deleteSet(id)
        }
    }

    fun startRest(seconds: Int) {
        viewModelScope.launch {
            _restSeconds.value = seconds
            _restTotalSeconds.value = seconds
            while (_restSeconds.value != null && _restSeconds.value!! > 0) {
                delay(1000)
                _restSeconds.value = (_restSeconds.value ?: 0) - 1
            }
            if (_restSeconds.value != null && _restSeconds.value!! <= 0) {
                _restSeconds.value = null
                _restTotalSeconds.value = null
            }
        }
    }

    fun addRest(seconds: Int) {
        _restSeconds.value = (_restSeconds.value ?: 0) + seconds
        _restTotalSeconds.value = (_restTotalSeconds.value ?: 0) + seconds
    }

    fun stopRest() {
        _restSeconds.value = null
        _restTotalSeconds.value = null
    }

    companion object {
        fun factory(repository: AppRepository, sessionId: Int): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SessionViewModel(repository, sessionId) as T
                }
            }
        }
    }
}

