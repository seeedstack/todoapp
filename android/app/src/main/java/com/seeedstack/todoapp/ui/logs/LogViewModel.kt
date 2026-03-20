package com.seeedstack.todoapp.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seeedstack.todoapp.data.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogUiState(
    val logs: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LogViewModel @Inject constructor(private val repo: LogRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    init { startAutoRefresh() }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) { loadLogs(); delay(30_000) }
        }
    }

    fun loadLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repo.getLogs()
                .onSuccess { logs -> _uiState.update { it.copy(logs = logs, isLoading = false, error = null) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
