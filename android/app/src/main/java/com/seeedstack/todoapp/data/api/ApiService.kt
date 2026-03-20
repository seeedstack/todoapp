package com.seeedstack.todoapp.data.api

import com.seeedstack.todoapp.data.api.models.*
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // Tasks
    @GET("api/tasks")
    suspend fun getTasks(): List<TaskDto>

    @POST("api/tasks")
    suspend fun createTask(@Body request: CreateTaskRequest): TaskDto

    @PUT("api/tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body request: UpdateTaskRequest): TaskDto

    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String)

    // Logs — returns plain list of strings
    @GET("api/logs")
    suspend fun getLogs(): List<String>

    // Admin — Users
    @GET("admin/api/users")
    suspend fun getUsers(): List<UserDto>

    @POST("admin/api/users")
    suspend fun createUser(@Body request: CreateUserRequest): UserDto

    @PUT("admin/api/users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body request: UpdateUserRequest): UserDto

    @DELETE("admin/api/users/{id}")
    suspend fun deleteUser(@Path("id") id: String)
}
