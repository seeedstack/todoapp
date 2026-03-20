package com.seeedstack.todoapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seeedstack.todoapp.data.api.models.TaskDto
import com.seeedstack.todoapp.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TaskFilter { ALL, PENDING, DONE, HIGH }

data class TaskListUiState(
    val tasks: List<TaskDto> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val editingTask: TaskDto? = null,
    val showForm: Boolean = false
)

val TaskListUiState.filtered: List<TaskDto> get() {
    val base = when (filter) {
        TaskFilter.PENDING -> tasks.filter { it.status == "pending" }
        TaskFilter.DONE    -> tasks.filter { it.status == "completed" }
        TaskFilter.HIGH    -> tasks.filter { it.priority == "high" }
        TaskFilter.ALL     -> tasks
    }
    val order = mapOf("high" to 0, "medium" to 1, "low" to 2)
    return base.sortedWith(compareBy({ if (it.status == "pending") 0 else 1 }, { order[it.priority] ?: 9 }))
}

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val repo: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    init { startAutoRefresh() }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) { loadTasks(); delay(30_000) }
        }
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repo.getTasks()
                .onSuccess { tasks -> _uiState.update { it.copy(tasks = tasks, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error") } }
        }
    }

    fun setFilter(f: TaskFilter) { _uiState.update { it.copy(filter = f) } }
    fun showCreate() { _uiState.update { it.copy(showForm = true, editingTask = null) } }
    fun showEdit(task: TaskDto) { _uiState.update { it.copy(showForm = true, editingTask = task) } }
    fun dismissForm() { _uiState.update { it.copy(showForm = false, editingTask = null) } }
    fun clearError() { _uiState.update { it.copy(error = null) } }

    fun saveTask(title: String, description: String?, priority: String) {
        val editing = _uiState.value.editingTask
        viewModelScope.launch {
            if (editing == null) {
                repo.createTask(title, description, priority)
                    .onSuccess { task -> _uiState.update { it.copy(tasks = listOf(task) + it.tasks, showForm = false) } }
                    .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
            } else {
                repo.updateTask(editing.id, title, description, priority, null)
                    .onSuccess { updated -> _uiState.update { s -> s.copy(
                        tasks = s.tasks.map { if (it.id == updated.id) updated else it },
                        showForm = false, editingTask = null
                    )} }
                    .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
            }
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            repo.deleteTask(id)
                .onSuccess { _uiState.update { it.copy(tasks = it.tasks.filter { t -> t.id != id }) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }
}
