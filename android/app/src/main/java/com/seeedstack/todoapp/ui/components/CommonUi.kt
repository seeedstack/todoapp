package com.seeedstack.todoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seeedstack.todoapp.ui.theme.*

@Composable
fun PriorityBadge(priority: String) {
    val (bg, text) = when (priority.lowercase()) {
        "high"   -> Color(0x1FEF4444) to HighColor
        "low"    -> Color(0x1F22C55E) to LowColor
        else     -> Color(0x1FF59E0B) to MedColor
    }
    Badge(label = priority.uppercase(), bg = bg, textColor = text)
}

@Composable
fun StatusBadge(status: String) {
    val (bg, text) = when (status.lowercase()) {
        "completed" -> Color(0x1F64748B) to MutedColor
        "error"     -> Color(0x1FEF4444) to HighColor
        else        -> Color(0x1F6366F1) to Color(0xFF818CF8)
    }
    Badge(label = when(status) { "completed" -> "DONE"; "error" -> "ERROR"; else -> "PENDING" }, bg = bg, textColor = text)
}

@Composable
fun RoleBadge(role: String) {
    val (bg, text) = if (role == "admin") Color(0x1FEF4444) to HighColor
                     else Color(0x1F6366F1) to Color(0xFF818CF8)
    Badge(label = role.uppercase(), bg = bg, textColor = text)
}

@Composable
private fun Badge(label: String, bg: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(label, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun LoadingOverlay() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AccentColor)
    }
}

@Composable
fun ErrorSnackbar(message: String, onDismiss: () -> Unit) {
    Snackbar(
        modifier = Modifier.padding(16.dp),
        action = { TextButton(onClick = onDismiss) { Text("Dismiss") } },
        containerColor = Color(0xFF1E1B2E)
    ) { Text(message, color = TextColor) }
}
