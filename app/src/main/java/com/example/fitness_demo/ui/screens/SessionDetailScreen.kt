package com.example.fitness_demo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitness_demo.data.AppRepository
import com.example.fitness_demo.data.SetEntry
import kotlinx.coroutines.launch

@Composable
fun SessionDetailScreen(
    sessionId: Int,
    repository: AppRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sets by repository.observeSetsForSession(sessionId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("会话详情 #$sessionId") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    scope.launch {
                        val exercise = repository.ensureExerciseByName("Push Up")
                        val nextSetNum = (sets.maxOfOrNull { it.setNumber } ?: 0) + 1
                        repository.addSet(
                            sessionId = sessionId,
                            exerciseId = exercise.id,
                            setNumber = nextSetNum,
                            reps = 10,
                            weightKg = null
                        )
                    }
                }) {
                    Text("添加示例组（俯卧撑 x10）")
                }
            }
            Divider()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
            ) {
                items(sets) { set ->
                    SetRow(set)
                }
            }
        }
    }
}

@Composable
private fun SetRow(set: SetEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "组 ${set.setNumber}")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "次数 ${set.reps}")
            Text(text = if (set.weightKg != null) "重量 ${set.weightKg}kg" else "自重")
        }
    }
    Divider()
}

