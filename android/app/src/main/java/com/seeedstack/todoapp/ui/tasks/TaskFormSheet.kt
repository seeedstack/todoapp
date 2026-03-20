package com.seeedstack.todoapp.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seeedstack.todoapp.data.api.models.TaskDto
import com.seeedstack.todoapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormSheet(
    editingTask: TaskDto?,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String?, priority: String) -> Unit
) {
    var title by remember(editingTask) { mutableStateOf(editingTask?.title ?: "") }
    var description by remember(editingTask) { mutableStateOf(editingTask?.description ?: "") }
    var priority by remember(editingTask) { mutableStateOf(editingTask?.priority ?: "medium") }
    var titleError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (editingTask == null) "Add Task" else "Edit Task",
                style = MaterialTheme.typography.titleMedium,
                color = TextColor
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text("Title") },
                isError = titleError,
                supportingText = if (titleError) {{ Text("Title is required", color = HighColor) }} else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth().height(88.dp),
                colors = fieldColors()
            )

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = priority.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Priority") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = fieldColors()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
                    containerColor = SurfaceColor) {
                    listOf("high", "medium", "low").forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt.replaceFirstChar { it.uppercase() }, color = TextColor) },
                            onClick = { priority = opt; expanded = false }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (title.isBlank()) { titleError = true; return@Button }
                        onSave(title.trim(), description.trim().ifEmpty { null }, priority)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                ) { Text(if (editingTask == null) "Add Task" else "Save Changes") }

                if (editingTask != null) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.wrapContentWidth()) {
                        Text("Cancel", color = MutedColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentColor,
    unfocusedBorderColor = BorderColor,
    focusedTextColor = TextColor,
    unfocusedTextColor = TextColor,
    focusedLabelColor = AccentColor,
    unfocusedLabelColor = MutedColor,
    cursorColor = AccentColor
)
