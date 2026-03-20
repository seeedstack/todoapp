package com.seeedstack.todoapp.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seeedstack.todoapp.data.api.models.UserDto
import com.seeedstack.todoapp.ui.components.*
import com.seeedstack.todoapp.ui.tasks.TopBar
import com.seeedstack.todoapp.ui.theme.*

@Composable
fun AdminScreen(onLogout: () -> Unit, viewModel: AdminViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopBar("Admin — Users", onLogout = onLogout) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showCreate, containerColor = AccentColor) {
                Icon(Icons.Default.Add, "Add user", tint = Color.White)
            }
        },
        containerColor = BgColor
    ) { padding ->
        if (state.isLoading && state.users.isEmpty()) {
            LoadingOverlay()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(state.users, key = { it.id }) { user ->
                    UserCard(
                        user = user,
                        onEdit = { viewModel.showEdit(user) },
                        onDelete = { viewModel.confirmDelete(user.id) }
                    )
                }
            }
        }

        if (state.showForm) {
            UserFormSheet(
                editingUser = state.editingUser,
                onDismiss = viewModel::dismissForm,
                onSave = viewModel::saveUser
            )
        }

        state.deleteConfirmUserId?.let { uid ->
            val user = state.users.find { it.id == uid }
            AlertDialog(
                onDismissRequest = viewModel::cancelDelete,
                title = { Text("Delete ${user?.username}?", color = TextColor) },
                text = { Text("This cannot be undone.", color = MutedColor) },
                confirmButton = {
                    TextButton(onClick = { viewModel.deleteUser(uid) }) {
                        Text("Delete", color = HighColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::cancelDelete) { Text("Cancel") }
                },
                containerColor = SurfaceColor
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
private fun UserCard(user: UserDto, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            Modifier.padding(14.dp, 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(user.username, fontWeight = FontWeight.Medium, color = TextColor)
                Text(user.createdAt.substring(0, 10), fontSize = 11.sp, color = MutedColor)
            }
            RoleBadge(user.role)
            TextButton(onClick = onDelete) {
                Text("Delete", color = HighColor, fontSize = 12.sp)
            }
        }
    }
}
