package com.seeedstack.todoapp.ui.logs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seeedstack.todoapp.ui.components.ErrorSnackbar
import com.seeedstack.todoapp.ui.components.LoadingOverlay
import com.seeedstack.todoapp.ui.tasks.TopBar
import com.seeedstack.todoapp.ui.theme.*

@Composable
fun LogScreen(onLogout: () -> Unit, viewModel: LogViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopBar("Activity Log", onLogout = onLogout)
        },
        containerColor = BgColor
    ) { padding ->
        if (state.isLoading && state.logs.isEmpty()) {
            LoadingOverlay()
        } else if (state.logs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No activity yet.", color = MutedColor)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(state.logs) { line ->
                    LogLine(line)
                }
            }
        }

        if (state.error != null) {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
                ErrorSnackbar(state.error!!, onDismiss = viewModel::clearError)
            }
        }
    }
}

@Composable
private fun LogLine(line: String) {
    val upper = line.uppercase()
    val msgColor = when {
        "CREATED" in upper    -> Color(0xFF818CF8)
        "COMPLETED" in upper || "COMPLETE" in upper -> Color(0xFF4ADE80)
        "DELETED" in upper    -> HighColor
        "ERROR" in upper      -> HighColor
        "PROCESSING" in upper -> MedColor
        else                  -> MutedColor
    }
    val tsEnd = line.indexOf(']') + 1
    val ts = if (tsEnd > 0) line.substring(0, tsEnd) else ""
    val msg = if (tsEnd > 0 && tsEnd < line.length) line.substring(tsEnd).trim() else line

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (ts.isNotEmpty()) Text(ts, color = Color(0xFF334155), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text(msg, color = msgColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
    HorizontalDivider(color = Color(0x08FFFFFF), thickness = 1.dp)
}
