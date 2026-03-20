package com.seeedstack.todoapp.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seeedstack.todoapp.data.api.models.UserDto
import com.seeedstack.todoapp.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val users: List<UserDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val editingUser: UserDto? = null,
    val showForm: Boolean = false,
    val deleteConfirmUserId: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(private val repo: AdminRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init { loadUsers() }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repo.getUsers()
                .onSuccess { users -> _uiState.update { it.copy(users = users, isLoading = false, error = null) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun showCreate() { _uiState.update { it.copy(showForm = true, editingUser = null) } }
    fun showEdit(user: UserDto) { _uiState.update { it.copy(showForm = true, editingUser = user) } }
    fun dismissForm() { _uiState.update { it.copy(showForm = false, editingUser = null) } }
    fun confirmDelete(userId: String) { _uiState.update { it.copy(deleteConfirmUserId = userId) } }
    fun cancelDelete() { _uiState.update { it.copy(deleteConfirmUserId = null) } }
    fun clearError() { _uiState.update { it.copy(error = null) } }

    fun saveUser(username: String, password: String?, role: String) {
        val editing = _uiState.value.editingUser
        viewModelScope.launch {
            if (editing == null) {
                repo.createUser(username, password ?: "", role)
                    .onSuccess { user -> _uiState.update { it.copy(users = it.users + user, showForm = false) } }
                    .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
            } else {
                repo.updateUser(editing.id, username.ifEmpty { null }, password, role)
                    .onSuccess { updated -> _uiState.update { s -> s.copy(
                        users = s.users.map { if (it.id == updated.id) updated else it },
                        showForm = false, editingUser = null
                    )} }
                    .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repo.deleteUser(userId)
                .onSuccess { _uiState.update { it.copy(users = it.users.filter { u -> u.id != userId }, deleteConfirmUserId = null) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message, deleteConfirmUserId = null) } }
        }
    }
}
