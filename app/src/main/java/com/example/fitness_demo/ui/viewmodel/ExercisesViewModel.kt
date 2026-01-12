package com.example.fitness_demo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitness_demo.data.AppRepository
import com.example.fitness_demo.data.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExercisesViewModel(
    private val repository: AppRepository
) : ViewModel() {

    val muscleGroups: List<String> = listOf("All", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Full Body", "Other")

    private val selectedGroupFlow = MutableStateFlow("All")
    private val searchQueryFlow = MutableStateFlow("")

    val exercises: StateFlow<List<Exercise>> =
        combine(selectedGroupFlow, searchQueryFlow) { g, q -> g to q }
            .flatMapLatest { (g, q) ->
                val base = if (g == "All") repository.observeExercises() else repository.observeExercisesByMuscle(g)
                base.map { list ->
                    val ql = q.trim().lowercase()
                    if (ql.isEmpty()) list else list.filter { it.name.lowercase().contains(ql) }
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addExercise(name: String) {
        viewModelScope.launch {
            repository.ensureExerciseByName(name)
        }
    }

    fun selectGroup(group: String) {
        selectedGroupFlow.value = group
    }

    val selectedGroup: StateFlow<String> = selectedGroupFlow
    val searchQuery: StateFlow<String> = searchQueryFlow
    fun setSearchQuery(q: String) { searchQueryFlow.value = q }

    companion object {
        fun factory(repository: AppRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ExercisesViewModel(repository) as T
                }
            }
        }
    }
}

