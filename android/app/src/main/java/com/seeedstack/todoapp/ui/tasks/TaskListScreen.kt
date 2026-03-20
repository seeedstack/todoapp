package com.seeedstack.todoapp.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seeedstack.todoapp.data.api.models.TaskDto
import com.seeedstack.todoapp.ui.components.*
import com.seeedstack.todoapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onLogout: () -> Unit,
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopBar("Task Dashboard", onLogout = onLogout)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::showCreate,
                containerColor = AccentColor
            ) { Icon(Icons.Default.Add, "Add task", tint = Color.White) }
        },
        containerColor = BgColor
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Stats + filters — unified header
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(SurfaceColor)
                    .padding(bottom = 4.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Pending", state.tasks.count { it.status == "pending" })
                    StatItem("Done", state.tasks.count { it.status == "completed" })
                    StatItem("Total", state.tasks.size)
                }
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskFilter.entries.forEach { f ->
                        FilterChip(
                            selected = state.filter == f,
                            onClick = { viewModel.setFilter(f) },
                            label = { Text(f.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            if (state.isLoading && state.tasks.isEmpty()) {
                LoadingOverlay()
            } else if (state.filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tasks here", color = MutedColor)
                }
            } else {
                androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                    isRefreshing = state.isLoading,
                    onRefresh = viewModel::loadTasks
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.filtered, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                onEdit = { viewModel.showEdit(task) },
                                onDelete = { viewModel.deleteTask(task.id) }
                            )
                        }
                    }
                }
            }
        }

        if (state.showForm) {
            TaskFormSheet(
                editingTask = state.editingTask,
                onDismiss = viewModel::dismissForm,
                onSave = viewModel::saveTask
            )
        }

        if (state.error != null) {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
                ErrorSnackbar(state.error!!, onDismiss = viewModel::clearError)
            }
        }
    }
}

@Composable
private fun TaskCard(task: TaskDto, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showActions by remember { mutableStateOf(false) }
    val isCompleted = task.status == "completed"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showActions = !showActions },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(Modifier.padding(14.dp, 12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    task.title,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Medium,
                    color = if (isCompleted) MutedColor else TextColor,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                PriorityBadge(task.priority)
                StatusBadge(task.status)
            }
            if (!task.description.isNullOrBlank()) {
                Text(
                    task.description,
                    color = MutedColor,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (!task.result.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0x0F22C55E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3322C55E))
                ) {
                    Text(task.result, color = Color(0xFF86EFAC), fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp))
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isCompleted && task.completedAt != null) "Completed ${fmtDate(task.completedAt)}"
                    else "Added ${fmtDate(task.createdAt)}",
                    fontSize = 11.sp, color = MutedColor
                )
                if (showActions) {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, "Edit", tint = MutedColor, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, "Delete", tint = HighColor, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextColor)
        Text(label, fontSize = 11.sp, color = MutedColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String, onLogout: () -> Unit) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        actions = {
            IconButton(onClick = onLogout) {
                Icon(Icons.Default.Logout, "Sign out", tint = MutedColor)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SurfaceColor,
            titleContentColor = TextColor
        ),
        windowInsets = WindowInsets(0)
    )
}

private fun fmtDate(iso: String): String {
    return try {
        iso.substring(0, 10) + " " + iso.substring(11, 16)
    } catch (_: Exception) { iso }
}
