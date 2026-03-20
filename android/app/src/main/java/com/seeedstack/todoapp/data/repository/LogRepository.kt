package com.seeedstack.todoapp.data.repository

import com.seeedstack.todoapp.data.api.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepository @Inject constructor(private val api: ApiService) {
    suspend fun getLogs(): Result<List<String>> = runCatching { api.getLogs() }
}
