package com.seeedstack.todoapp.data.repository

import com.seeedstack.todoapp.data.api.ApiService
import com.seeedstack.todoapp.data.api.models.CreateTaskRequest
import com.seeedstack.todoapp.data.api.models.TaskDto
import com.seeedstack.todoapp.data.api.models.UpdateTaskRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(private val api: ApiService) {
    suspend fun getTasks(): Result<List<TaskDto>> = runCatching { api.getTasks() }
    suspend fun createTask(title: String, description: String?, priority: String): Result<TaskDto> =
        runCatching { api.createTask(CreateTaskRequest(title, description, priority)) }
    suspend fun updateTask(id: String, title: String?, description: String?, priority: String?, status: String?): Result<TaskDto> =
        runCatching { api.updateTask(id, UpdateTaskRequest(title, description, priority, status)) }
    suspend fun deleteTask(id: String): Result<Unit> = runCatching { api.deleteTask(id) }
}
