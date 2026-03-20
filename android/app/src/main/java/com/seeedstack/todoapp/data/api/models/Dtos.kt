package com.seeedstack.todoapp.data.api.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(@SerializedName("access_token") val accessToken: String)

data class TaskDto(
    val id: String,
    val title: String,
    val description: String?,
    val priority: String,
    val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("completed_at") val completedAt: String?,
    val result: String?
)

data class CreateTaskRequest(val title: String, val description: String?, val priority: String)

data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val priority: String? = null,
    val status: String? = null
)

data class UserDto(
    val id: String,
    val username: String,
    val role: String,
    @SerializedName("created_at") val createdAt: String
)

data class CreateUserRequest(val username: String, val password: String, val role: String)

data class UpdateUserRequest(
    val username: String? = null,
    val password: String? = null,
    val role: String? = null
)
