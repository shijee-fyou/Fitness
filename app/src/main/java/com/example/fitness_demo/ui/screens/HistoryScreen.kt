package com.example.fitness_demo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.AssistChip
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitness_demo.data.AppRepository
import com.example.fitness_demo.data.TrainingSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import com.example.fitness_demo.ui.theme.Dimens
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

private data class DaySummary(val sessionCount: Int, val totalVolumeKg: Float, val topGroups: List<String>)
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
        q.isEmpty() || label.contains(q) || (s.note?.contains(q) == true) || (s.theme?.contains(q) == true)
    }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    var calendarMode by remember { mutableStateOf(true) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
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
                actions = {
                    TextButton(onClick = { calendarMode = !calendarMode }) {
                        Text(if (calendarMode) "列表" else "日历")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            val s = repository.startNewSession()
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenSession(s.id)
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "新建训练")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        if (!calendarMode) {
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
                        .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.ChipSpacing)) {
                        OutlinedTextField(
                            value = query.value,
                            onValueChange = { query.value = it },
                            label = { Text("搜索记录（时间/备注/主题）") },
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
                if (filtered.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(Dimens.ScreenPadding)
                        ) {
                            Text("暂无记录", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
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
                                        .padding(horizontal = Dimens.ScreenPadding),
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
        } else {
            // 日历视图
                val monthStart = currentMonth.atDay(1)
                val daysInMonth = currentMonth.lengthOfMonth()
                // 周一=1 … 周日=7，转为 0..6（周一在前）
                val firstWeekIndex = (monthStart.dayOfWeek.value + 6) % 7
                val totalCells = firstWeekIndex + daysInMonth
                val rows = ((totalCells + 6) / 7) * 7
                val dateToSessions = remember(sessions) {
                    sessions.groupBy { s ->
                        Instant.ofEpochMilli(s.startTimeMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                }

                // 计算每日摘要（会话数/训练量/主肌群TopN）
                var summaries by remember { mutableStateOf<Map<LocalDate, DaySummary>>(emptyMap()) }
                LaunchedEffect(sessions) {
                    val map = mutableMapOf<LocalDate, DaySummary>()
                    val grouped = sessions.groupBy { s -> Instant.ofEpochMilli(s.startTimeMillis).atZone(ZoneId.systemDefault()).toLocalDate() }
                    for ((d, listS) in grouped) {
                        var vol = 0f
                        val groupCounts = mutableMapOf<String, Int>()
                        for (s in listS) {
                            val sets = repository.getSetsForSessionOnce(s.id)
                            vol += sets.fold(0f) { acc, st -> acc + (st.weightKg ?: 0f) * st.reps }
                            val g = s.theme?.substringBefore(" ")?.lowercase() ?: "other"
                            groupCounts[g] = (groupCounts[g] ?: 0) + 1
                        }
                        val top = groupCounts.entries.sortedByDescending { it.value }.take(3).map { it.key }
                        map[d] = DaySummary(sessionCount = listS.size, totalVolumeKg = vol, topGroups = top)
                    }
                    summaries = map
                }

                var daySheetOpen by remember { mutableStateOf(false) }
                var sheetDate by remember { mutableStateOf<LocalDate?>(null) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.ChipSpacing)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "上个月")
                        }
                        Text(
                            text = "${currentMonth.year}年${currentMonth.monthValue}月",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { 
                                currentMonth = YearMonth.now()
                                selectedDate = LocalDate.now()
                            }) { Text("今天") }
                            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "下个月")
                            }
                        }
                    }
                    // 本周快捷条（可点击定位/打开）
                    val weekStart = selectedDate.minusDays(((selectedDate.dayOfWeek.value + 6) % 7).toLong())
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.ChipSpacing),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (0..6).forEach { i ->
                            val d = weekStart.plusDays(i.toLong())
                            val isSel = d == selectedDate
                            val dotColors = (dateToSessions[d] ?: emptyList()).mapNotNull { themeColor(it.theme) }.distinct().take(3)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedDate = d
                                        currentMonth = YearMonth.of(d.year, d.monthValue)
                                        sheetDate = d
                                        daySheetOpen = true
                                    }
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    dotColors.forEach { c ->
                                        androidx.compose.foundation.Canvas(modifier = Modifier.size(4.dp)) { drawCircle(color = c) }
                                    }
                                }
                                Text(
                                    "${d.dayOfMonth}",
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    style = if (isSel) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    val weekdayNames = listOf("一","二","三","四","五","六","日")
                    val gridSpacing = 4.dp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(gridSpacing)
                    ) {
                        weekdayNames.forEach {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    // 切月指示点（当前月高亮）
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 简洁三点：上一月/当前/下一月，当前高亮
                        listOf(false, true, false).forEach { selected ->
                            val dotColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier
                                    .size(if (selected) 8.dp else 6.dp)
                                    .padding(horizontal = 4.dp)
                            ) {
                                drawCircle(color = dotColor)
                            }
                        }
                    }
                    val density = LocalDensity.current
                    var dragAccum = 0f
                    val thresholdPx = with(density) { 60.dp.toPx() }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(currentMonth) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        when {
                                            dragAccum > thresholdPx -> currentMonth = currentMonth.minusMonths(1)
                                            dragAccum < -thresholdPx -> currentMonth = currentMonth.plusMonths(1)
                                        }
                                        dragAccum = 0f
                                    },
                                    onHorizontalDrag = { _, dragAmount ->
                                        dragAccum += dragAmount
                                    }
                                )
                            }
                    ) {
                        val monthIndex = currentMonth.year * 12 + currentMonth.monthValue
                        var lastMonthIndex by remember { mutableStateOf(monthIndex) }
                        val dir = if (monthIndex >= lastMonthIndex) 1 else -1
                        LaunchedEffect(currentMonth) { lastMonthIndex = monthIndex }
                        AnimatedContent(
                            targetState = currentMonth,
                            transitionSpec = {
                                slideInHorizontally(initialOffsetX = { it * dir }) togetherWith
                                slideOutHorizontally(targetOffsetX = { -it * dir })
                            },
                            label = "month-slide"
                        ) { m ->
                            val mStart = m.atDay(1)
                            val mDays = m.lengthOfMonth()
                            val mFirstWeekIndex = (mStart.dayOfWeek.value + 6) % 7
                            val mTotalCells = mFirstWeekIndex + mDays
                            val mRows = ((mTotalCells + 6) / 7) * 7
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(7),
                                verticalArrangement = Arrangement.spacedBy(gridSpacing),
                                horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(mRows) { index ->
                                    val dayNumber = index - mFirstWeekIndex + 1
                                    if (dayNumber in 1..mDays) {
                                        val date = mStart.withDayOfMonth(dayNumber)
                                            DayCell(
                                            date = date,
                                            sessions = dateToSessions[date] ?: emptyList(),
                                            onOpenSession = onOpenSession,
                                                onClickDay = { d ->
                                                selectedDate = d
                                                sheetDate = d
                                                daySheetOpen = true
                                            },
                                            onLongPressDay = { d ->
                                                sheetDate = d
                                                daySheetOpen = true
                                                },
                                                selected = (date == selectedDate),
                                                summary = summaries[date]
                                        )
                                    } else {
                                        Box(modifier = Modifier
                                            .height(72.dp)
                                            .fillMaxWidth())
                                    }
                                }
                            }
                        }
                    }

                    if (daySheetOpen && sheetDate != null) {
                        val list = dateToSessions[sheetDate] ?: emptyList()
                        val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
                        androidx.compose.material3.ModalBottomSheet(
                            onDismissRequest = { daySheetOpen = false },
                            sheetState = sheetState
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Header with weekday
                                val wd = sheetDate!!.dayOfWeek.value
                                val wdLabel = listOf("周一","周二","周三","周四","周五","周六","周日")[ (wd + 6) % 7 ]
                                Text("${sheetDate!!.year}年${sheetDate!!.monthValue}月${sheetDate!!.dayOfMonth}日 · ${wdLabel}", style = MaterialTheme.typography.titleMedium)
                                if (list.isEmpty()) {
                                    Text("无训练记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    // 统计缓存：sessionId -> Pair(totalSets, totalVolumeKg)
                                    var stats by remember { mutableStateOf<Map<Int, Pair<Int, Float>>>(emptyMap()) }
                                    LaunchedEffect(list) {
                                        val map = mutableMapOf<Int, Pair<Int, Float>>()
                                        list.forEach { sess ->
                                            val sets = repository.getSetsForSessionOnce(sess.id)
                                            val totalSets = sets.size
                                            val volume = sets.fold(0f) { acc, st -> acc + (st.weightKg ?: 0f) * st.reps }
                                            map[sess.id] = totalSets to volume
                                        }
                                        stats = map
                                    }
                                    val totalSets = stats.values.sumOf { it.first }
                                    val totalVolume = stats.values.fold(0f) { acc, p -> acc + p.second }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        AssistChip(onClick = {}, label = { Text("会话 ${list.size}") })
                                        AssistChip(onClick = {}, label = { Text("组数 $totalSets") })
                                        AssistChip(onClick = {}, label = { Text("训练量 ${"%.0f".format(totalVolume)}kg") })
                                    }
                                    // 近7天训练次数微图（按 sessions 数量）
                                    val last7 = (0..6).map { i -> sheetDate!!.minusDays((6 - i).toLong()) }
                                    val counts = last7.map { d -> (dateToSessions[d] ?: emptyList()).size }
                                    val max = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 4.dp)) {
                                        counts.forEachIndexed { idx, c ->
                                            val h = (c.toFloat() / max.toFloat()) * 48f
                                            Box(modifier = Modifier
                                                .size(width = 10.dp, height = (h.dp).coerceAtLeast(4.dp))
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                                            )
                                        }
                                    }
                                    // 列表
                                    val actionScope = rememberCoroutineScope()
                                    list.forEach { s ->
                                        androidx.compose.material3.ElevatedCard(
                                            onClick = {
                                                daySheetOpen = false
                                                onOpenSession(s.id)
                                            },
                                            elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(defaultElevation = Dimens.CardElevationLow)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    val title = s.theme ?: (s.note?.takeIf { it.isNotBlank() } ?: "训练 #${s.id}")
                                                    Text(title, style = MaterialTheme.typography.bodyMedium)
                                                    val value = stats[s.id]
                                                    val setsCount = value?.first
                                                    val volume = value?.second
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                        if (setsCount != null) {
                                                            AssistChip(onClick = {}, label = { Text("组数 ${setsCount}") })
                                                        }
                                                        if (volume != null) {
                                                            AssistChip(onClick = {}, label = { Text("总量 ${"%.0f".format(volume)}kg") })
                                                        }
                                                    }
                                                }
                                                val dot = themeColor(s.theme)
                                                if (dot != null) {
                                                    androidx.compose.foundation.Canvas(modifier = Modifier.size(10.dp)) {
                                                        drawCircle(color = dot)
                                                    }
                                                }
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                                TextButton(onClick = {
                                                    daySheetOpen = false
                                                    onOpenSession(s.id)
                                                }) { Text("打开") }
                                                TextButton(onClick = {
                                                    actionScope.launch {
                                                        val newSession = repository.startNewSession(note = s.note ?: s.theme)
                                                        val sets = repository.getSetsForSessionOnce(s.id)
                                                        sets.forEach { st ->
                                                            repository.addSet(
                                                                sessionId = newSession.id,
                                                                exerciseId = st.exerciseId,
                                                                setNumber = st.setNumber,
                                                                reps = st.reps,
                                                                weightKg = st.weightKg,
                                                                rpe = st.rpe
                                                            )
                                                        }
                                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        onOpenSession(newSession.id)
                                                    }
                                                }) { Text("复制到今天") }
                                                TextButton(onClick = {
                                                    actionScope.launch {
                                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        repository.deleteSessionById(s.id)
                                                        snackbarHostState.showSnackbar("已删除会话", withDismissAction = true, duration = SnackbarDuration.Short)
                                                    }
                                                }) { Text("删除") }
                                            }
                                        }
                                    }
                                }
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
    androidx.compose.material3.ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = { Text(text = subtitle) },
        trailingContent = {
            val theme = session.theme
            if (!theme.isNullOrBlank()) {
                AssistChip(onClick = {}, label = { Text(theme) })
            }
        },
        modifier = modifier
            .clickable { onOpenSession(session.id) }
            .animateContentSize()
    )
    HorizontalDivider()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayCell(
    date: LocalDate,
    sessions: List<TrainingSession>,
    onOpenSession: (Int) -> Unit,
    onLongPressDay: (LocalDate) -> Unit = {},
    onClickDay: (LocalDate) -> Unit = {},
    selected: Boolean = false,
    summary: DaySummary? = null
) {
    val isToday = date == LocalDate.now()
    androidx.compose.material3.Surface(
        tonalElevation = if (isToday || selected) 2.dp else 0.dp,
        shadowElevation = if (isToday || selected) 2.dp else 0.dp,
        shape = androidx.compose.material3.MaterialTheme.shapes.small,
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
            .padding(2.dp)
            .let { m ->
                if (selected) m.border(1.25.dp, MaterialTheme.colorScheme.primary, androidx.compose.material3.MaterialTheme.shapes.small)
                else m
            }
            .combinedClickable(
                onClick = { onClickDay(date) },
                onLongClick = { onLongPressDay(date) }
            )
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "${date.dayOfMonth}",
            modifier = Modifier.fillMaxWidth(),
            style = if (selected) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        val vol = (summary?.totalVolumeKg ?: 0f).coerceAtLeast(0f)
        if (vol > 0f) {
            androidx.compose.material3.Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${"%.0f".format(vol)}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                }
            }
        }
    }}
}

@Composable
private fun themeColor(theme: String?): Color? {
    if (theme.isNullOrBlank()) return null
    val key = theme.substringBefore(" ").lowercase()
    return when (key) {
        "chest" -> MaterialTheme.colorScheme.primary
        "back" -> MaterialTheme.colorScheme.tertiary
        "legs" -> MaterialTheme.colorScheme.secondary
        "shoulders" -> MaterialTheme.colorScheme.error
        "arms" -> MaterialTheme.colorScheme.inversePrimary
        "core" -> MaterialTheme.colorScheme.primaryContainer
        "full" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.outline
    }
}

private fun groupShortLabel(key: String): String {
    return when (key.lowercase()) {
        "chest" -> "胸部"
        "back" -> "背部"
        "legs" -> "腿部"
        "shoulders" -> "肩部"
        "arms" -> "手臂"
        "core" -> "核心"
        "full" -> "全身"
        else -> "其"
    }
}
