package com.example.fitness_demo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitness_demo.data.AppRepository
import com.example.fitness_demo.data.TrainingSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    repository: AppRepository,
    onBack: () -> Unit,
    onOpenSession: (Int) -> Unit
) {
    val sessions by repository.observeSessions().collectAsState(initial = emptyList())
    Scaffold(
        topBar = { TopAppBar(title = { Text("训练记录") }) }
    ) { padding ->
        if (sessions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("暂无记录")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(sessions) { session ->
                    SessionRow(session = session, onOpenSession = onOpenSession)
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: TrainingSession, onOpenSession: (Int) -> Unit) {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val label = formatter.format(Date(session.startTimeMillis))
    Column(
        modifier = Modifier
            .clickable { onOpenSession(session.id) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = "会话 #${session.id}")
        Text(text = label)
    }
}

