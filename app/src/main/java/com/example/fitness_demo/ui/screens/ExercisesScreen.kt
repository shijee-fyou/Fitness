package com.example.fitness_demo.ui.screens

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fitness_demo.ui.theme.Dimens
import com.example.fitness_demo.data.AppRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitness_demo.ui.viewmodel.ExercisesViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExercisesScreen(
    repository: AppRepository,
    onBack: () -> Unit,
    onOpenExercise: (Int) -> Unit
){
    val vm: ExercisesViewModel = viewModel(factory = ExercisesViewModel.factory(repository))
    val exercises by vm.exercises.collectAsState()
    val selectedGroup by vm.selectedGroup.collectAsState()
    val groups = vm.muscleGroups
    var newName by remember { mutableStateOf("") }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = { LargeTopAppBar(title = { Text("训练动作") }, scrollBehavior = scrollBehavior) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.ScreenPadding)
        ) {
            ElevatedCard(
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = Dimens.CardElevationMed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Dimens.CardPadding), verticalArrangement = Arrangement.spacedBy(Dimens.ChipSpacing)) {
                    val query by vm.searchQuery.collectAsState()
                    OutlinedTextField(
                        value = query,
                        onValueChange = { vm.setSearchQuery(it) },
                        label = { Text("搜索动作") },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "清空",
                                    modifier = Modifier
                                        .height(24.dp)
                                        .width(24.dp)
                                        .clickable { vm.setSearchQuery("") }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val haptics = LocalHapticFeedback.current
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("新动作") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "添加",
                                    modifier = Modifier
                                        .clickable {
                                            if (newName.isNotBlank()) {
                                                vm.addExercise(newName.trim())
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                newName = ""
                                            }
                                        }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.ChipSpacing))
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.ChipSpacing),
                horizontalArrangement = Arrangement.spacedBy(Dimens.ChipSpacing),
                verticalArrangement = Arrangement.spacedBy(Dimens.ChipSpacing)
            ) {
                groups.forEach { g ->
                    FilterChip(
                        selected = selectedGroup == g,
                        onClick = { vm.selectGroup(g) },
                        label = { Text(g) }
                    )
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 180.dp),
                verticalArrangement = Arrangement.spacedBy(Dimens.ListSpacing),
                horizontalArrangement = Arrangement.spacedBy(Dimens.ListSpacing),
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = Dimens.SectionSpacing)
            ) {
                items(exercises, key = { it.id }) { ex ->
                    val interaction = remember { MutableInteractionSource() }
                    val pressed by interaction.collectIsPressedAsState()
                    val scale = if (pressed) 0.98f else 1f
                    ElevatedCard(
                        onClick = { onOpenExercise(ex.id) },
                        interactionSource = interaction,
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = Dimens.CardElevationLow),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    ) {
                        Column(modifier = Modifier.padding(Dimens.CardPadding), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    ex.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CompactTag(label = abbreviateGroup(ex.muscleGroup))
                                CompactTag(label = difficultyFor(ex.name))
                            }
                            ex.description?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
private fun difficultyFor(name: String): String {
    val n = name.lowercase()
    return when {
        listOf("snatch", "clean and jerk", "clean and press").any { it in n } -> "Hard"
        listOf("deadlift", "squat", "bench", "overhead press").any { it in n } -> "Hard"
        listOf("row", "pulldown", "pull up", "chin up", "hip thrust").any { it in n } -> "Medium"
        listOf("curl", "raise", "plank", "crunch", "extension", "pressdown").any { it in n } -> "Easy"
        else -> "Medium"
    }
}

private fun abbreviateGroup(group: String): String = when (group.lowercase()) {
    "shoulders" -> "Shldr"
    "full body" -> "Full"
    else -> group
}

@Composable
private fun CompactTag(label: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

