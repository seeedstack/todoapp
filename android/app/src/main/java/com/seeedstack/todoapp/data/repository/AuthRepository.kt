package com.seeedstack.todoapp.data.repository

import com.seeedstack.todoapp.data.api.ApiService
import com.seeedstack.todoapp.data.api.models.LoginRequest
import com.seeedstack.todoapp.data.local.TokenStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore
) {
    suspend fun login(username: String, password: String): Result<Unit> = runCatching {
        val response = api.login(LoginRequest(username, password))
        tokenStore.saveToken(response.accessToken)
    }

    fun logout() = tokenStore.clear()
    fun hasToken() = tokenStore.hasToken()
    fun isAdmin() = tokenStore.isAdmin()
}
