package com.example.fitness_demo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.example.fitness_demo.data.AppRepository
import com.example.fitness_demo.data.TrainingSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    repository: AppRepository,
    onBack: () -> Unit,
    onOpenSession: (Int) -> Unit
) {
    val sessions by repository.observeSessions().collectAsState(initial = emptyList())
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var query = remember { mutableStateOf("") }
    val filtered = sessions.filter { s ->
        val label = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(s.startTimeMillis))
        val q = query.value.trim()
        q.isEmpty() || label.contains(q) || (s.note?.contains(q) == true)
    }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("训练记录") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        if (sessions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(com.example.fitness_demo.ui.theme.Dimens.ScreenPadding)
            ) {
                Text("暂无记录", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val listState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                state = listState
            ) {
                stickyHeader {
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = com.example.fitness_demo.ui.theme.Dimens.ScreenPadding, vertical = com.example.fitness_demo.ui.theme.Dimens.ChipSpacing)) {
                        OutlinedTextField(
                            value = query.value,
                            onValueChange = { query.value = it },
                            label = { Text("搜索记录（时间/备注）") },
                            trailingIcon = {
                                if (query.value.isNotBlank()) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "清空",
                                        modifier = Modifier.clickable { query.value = "" }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                itemsIndexed(filtered, key = { _, s -> s.id }) { index, session ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                scope.launch {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    repository.deleteSessionById(session.id)
                                    val result = snackbarHostState.showSnackbar(
                                        message = "已删除训练记录",
                                        actionLabel = "撤销",
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                        repository.recreateSession(session)
                                    }
                                }
                                true
                            } else {
                                false
                            }
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = com.example.fitness_demo.ui.theme.Dimens.ScreenPadding),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "删除",
                                    tint = Color.Red
                                )
                            }
                        }
                    ) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { (index % 6) * 10 })
                        ) {
                            SessionRow(session = session, onOpenSession = onOpenSession, modifier = Modifier.animateItemPlacement())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: TrainingSession, onOpenSession: (Int) -> Unit, modifier: Modifier = Modifier) {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val label = formatter.format(Date(session.startTimeMillis))
    val title = session.note?.takeIf { it.isNotBlank() } ?: label
    val subtitle = if (title == label) "训练 #${session.id}" else "训练 #${session.id} • $label"
    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = { Text(text = subtitle) },
        modifier = modifier
            .clickable { onOpenSession(session.id) }
            .animateContentSize()
    )
    HorizontalDivider()
}

