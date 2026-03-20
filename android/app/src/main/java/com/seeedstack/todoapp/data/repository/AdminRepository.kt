package com.seeedstack.todoapp.data.repository

import com.seeedstack.todoapp.data.api.ApiService
import com.seeedstack.todoapp.data.api.models.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(private val api: ApiService) {
    suspend fun getUsers(): Result<List<UserDto>> = runCatching { api.getUsers() }
    suspend fun createUser(username: String, password: String, role: String): Result<UserDto> =
        runCatching { api.createUser(CreateUserRequest(username, password, role)) }
    suspend fun updateUser(id: String, username: String?, password: String?, role: String?): Result<UserDto> =
        runCatching { api.updateUser(id, UpdateUserRequest(username, password?.ifEmpty { null }, role)) }
    suspend fun deleteUser(id: String): Result<Unit> = runCatching { api.deleteUser(id) }
}
