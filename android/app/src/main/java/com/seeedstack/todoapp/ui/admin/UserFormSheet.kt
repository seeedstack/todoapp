package com.seeedstack.todoapp.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.seeedstack.todoapp.data.api.models.UserDto
import com.seeedstack.todoapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormSheet(
    editingUser: UserDto?,
    onDismiss: () -> Unit,
    onSave: (username: String, password: String?, role: String) -> Unit
) {
    var username by remember(editingUser) { mutableStateOf(editingUser?.username ?: "") }
    var password by remember { mutableStateOf("") }
    var role by remember(editingUser) { mutableStateOf(editingUser?.role ?: "user") }
    var usernameError by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = SurfaceColor) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (editingUser == null) "Create User" else "Edit User",
                style = MaterialTheme.typography.titleMedium, color = TextColor
            )

            OutlinedTextField(
                value = username, onValueChange = { username = it; usernameError = false },
                label = { Text("Username") }, isError = usernameError, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = if (usernameError) {{ Text("Username is required", color = HighColor) }} else null,
                colors = fieldColors()
            )

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text(if (editingUser == null) "Password" else "New Password (leave blank to keep)") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors()
            )

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = role.replaceFirstChar { it.uppercase() }, onValueChange = {}, readOnly = true,
                    label = { Text("Role") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), colors = fieldColors()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
                    containerColor = SurfaceColor) {
                    listOf("user", "admin").forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt.replaceFirstChar { it.uppercase() }, color = TextColor) },
                            onClick = { role = opt; expanded = false }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (username.isBlank()) { usernameError = true; return@Button }
                        onSave(username.trim(), password.ifEmpty { null }, role)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                ) { Text(if (editingUser == null) "Create" else "Save") }

                if (editingUser != null) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel", color = MutedColor) }
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentColor, unfocusedBorderColor = BorderColor,
    focusedTextColor = TextColor, unfocusedTextColor = TextColor,
    focusedLabelColor = AccentColor, unfocusedLabelColor = MutedColor, cursorColor = AccentColor
)
