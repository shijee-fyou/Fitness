package com.example.fitness_demo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitness_demo.data.AppRepository
import com.example.fitness_demo.data.Exercise
import com.example.fitness_demo.data.SetEntry
import com.example.fitness_demo.data.TrainingSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class SessionSummary(
    val totalSets: Int,
    val totalReps: Int,
    val totalVolumeKg: Float,
    val theme: String?
)

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

    private val _rpeText = MutableStateFlow("")
    val rpeText: StateFlow<String> = _rpeText.asStateFlow()

    private val _restSeconds = MutableStateFlow<Int?>(null)
    val restSeconds: StateFlow<Int?> = _restSeconds.asStateFlow()

    private val _restTotalSeconds = MutableStateFlow<Int?>(null)
    val restTotalSeconds: StateFlow<Int?> = _restTotalSeconds.asStateFlow()

    private val _session = MutableStateFlow<TrainingSession?>(null)
    val session: StateFlow<TrainingSession?> = _session.asStateFlow()

    enum class UnitSystem { KG, LB }
    private val _unitSystem = MutableStateFlow(UnitSystem.KG)
    val unitSystem: StateFlow<UnitSystem> = _unitSystem.asStateFlow()

    private val _recentReps = MutableStateFlow<List<Int>>(emptyList())
    val recentReps: StateFlow<List<Int>> = _recentReps.asStateFlow()

    private val _recentWeightsKg = MutableStateFlow<List<Float>>(emptyList())
    val recentWeightsKg: StateFlow<List<Float>> = _recentWeightsKg.asStateFlow()

    fun toggleUnit() {
        _unitSystem.value = if (_unitSystem.value == UnitSystem.KG) UnitSystem.LB else UnitSystem.KG
    }

    private fun rememberRecentReps(value: Int) {
        val list = _recentReps.value.toMutableList()
        if (!list.contains(value)) list.add(0, value)
        _recentReps.value = list.take(8)
    }
    private fun rememberRecentWeightKg(value: Float?) {
        val v = value ?: return
        if (v <= 0f) return
        val list = _recentWeightsKg.value.toMutableList()
        if (!list.contains(v)) list.add(0, v)
        _recentWeightsKg.value = list.take(10)
    }

    init {
        viewModelScope.launch {
            _session.value = repository.getSessionById(sessionId)
        }
    }

    fun selectExercise(id: Int?) { _selectedExerciseId.value = id }
    fun setRepsText(t: String) { _repsText.value = t }
    fun setWeightText(t: String) { _weightText.value = t }
    fun setRpeText(t: String) { _rpeText.value = t }

    fun addSet() {
        val exId = _selectedExerciseId.value ?: return
        val reps = _repsText.value.toIntOrNull() ?: return
        val weight = _weightText.value.toFloatOrNull()
        val rpe = _rpeText.value.toFloatOrNull()
        val nextSetNum = (sets.value.maxOfOrNull { it.setNumber } ?: 0) + 1
        viewModelScope.launch {
            repository.addSet(
                sessionId = sessionId,
                exerciseId = exId,
                setNumber = nextSetNum,
                reps = reps,
                weightKg = weight,
                rpe = rpe
            )
        }
    }

    fun addRest(seconds: Int) {
        val cur = _restSeconds.value
        if (cur == null) {
            _restSeconds.value = seconds
            _restTotalSeconds.value = seconds
            viewModelScope.launch {
                while (true) {
                    val remain = _restSeconds.value ?: break
                    if (remain <= 0) {
                        _restSeconds.value = null
                        _restTotalSeconds.value = null
                        break
                    }
                    delay(1000)
                    _restSeconds.value = ( _restSeconds.value ?: 0 ) - 1
                }
            }
        } else {
            _restSeconds.value = cur + seconds
            _restTotalSeconds.value = (_restTotalSeconds.value ?: 0) + seconds
        }
    }

    fun stopRest() {
        _restSeconds.value = null
        _restTotalSeconds.value = null
    }

    fun currentSummary(): SessionSummary {
        val list = sets.value
        val totalSets = list.size
        val totalReps = list.sumOf { it.reps }
        val totalVolume = list.fold(0f) { acc, s -> acc + (s.weightKg ?: 0f) * s.reps }
        // 主题依据 complete 后会写入 session，但这里也尝试即时报一个估计
        val exMap = exercises.value.associateBy { it.id }
        val counts = mutableMapOf<String, Int>()
        for (s in list) {
            val mg = exMap[s.exerciseId]?.muscleGroup ?: continue
            counts[mg] = (counts[mg] ?: 0) + 1
        }
        val top = counts.maxByOrNull { it.value }?.key
        val theme = top?.let { "$it 训练" }
        return SessionSummary(totalSets, totalReps, totalVolume, theme)
    }

    fun presetFromLastOfSelectedExercise() {
        val exId = _selectedExerciseId.value ?: return
        val last = sets.value.lastOrNull { it.exerciseId == exId } ?: return
        _repsText.value = last.reps.toString()
        _weightText.value = last.weightKg?.let { if (it == 0f) "" else it.toString() } ?: ""
    }

    fun copyLastAndAdd() {
        val exId = _selectedExerciseId.value ?: return
        val last = sets.value.lastOrNull { it.exerciseId == exId } ?: return
        _repsText.value = last.reps.toString()
        _weightText.value = last.weightKg?.let { if (it == 0f) "" else it.toString() } ?: ""
        addSet()
    }

    suspend fun bestAndAverageForExercise(exerciseId: Int): Pair<SetEntry?, Pair<Float, Float>?> {
        val lastSets = repository.getLastSetsForExercise(exerciseId, limit = 20)
        if (lastSets.isEmpty()) return null to null
        val best = lastSets.maxByOrNull { (it.weightKg ?: 0f) * it.reps }
        val avgWeight = lastSets.map { it.weightKg ?: 0f }.average().toFloat()
        val avgReps = lastSets.map { it.reps }.average().toFloat()
        return best to (avgWeight to avgReps)
    }

    fun trendSuggestionForCurrent(): Pair<Int?, Float?> {
        val exId = _selectedExerciseId.value ?: return null to null
        val lastThree = sets.value.filter { it.exerciseId == exId }.takeLast(3)
        if (lastThree.size < 2) return null to null
        val repsDelta = lastThree.zipWithNext().map { (a, b) -> b.reps - a.reps }.average()
        val weightDelta = lastThree.zipWithNext().map { (a, b) -> (b.weightKg ?: 0f) - (a.weightKg ?: 0f) }.average()
        val repsSugg = if (kotlin.math.abs(repsDelta) >= 0.5) repsDelta.toInt() else null
        val weightSugg = if (kotlin.math.abs(weightDelta) >= 0.25f) weightDelta.toFloat() else null
        return repsSugg to weightSugg
    }

    fun deleteSet(id: Int) {
        viewModelScope.launch {
            repository.deleteSet(id)
        }
    }

    fun completeSession(onCompleted: (() -> Unit)? = null) {
        viewModelScope.launch {
            // 统计该会话中涉及到的肌群，取出现频次最高的肌群作为主题
            val setsNow = sets.value
            val exMap = exercises.value.associateBy { it.id }
            val counts = mutableMapOf<String, Int>()
            for (s in setsNow) {
                val mg = exMap[s.exerciseId]?.muscleGroup ?: continue
                counts[mg] = (counts[mg] ?: 0) + 1
            }
            val top = counts.maxByOrNull { it.value }?.key
            val theme = top?.let { "$it 训练" } ?: "训练完成"
            repository.completeSession(sessionId, theme)
        _session.value = repository.getSessionById(sessionId)
            stopRest()
            onCompleted?.invoke()
        }
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

