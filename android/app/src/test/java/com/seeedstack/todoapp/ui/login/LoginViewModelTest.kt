package com.seeedstack.todoapp.ui.login

import com.seeedstack.todoapp.data.repository.AuthRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: AuthRepository
    private lateinit var vm: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk()
        vm = LoginViewModel(repo)
    }

    @After
    fun teardown() { Dispatchers.resetMain() }

    @Test
    fun `login with blank fields sets error`() {
        vm.login()
        assertEquals("Username and password are required", vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoggedIn)
    }

    @Test
    fun `successful login sets isLoggedIn true`() = runTest {
        coEvery { repo.login(any(), any()) } returns Result.success(Unit)
        vm.onUsernameChange("admin")
        vm.onPasswordChange("pass")
        vm.login()
        assertTrue(vm.uiState.value.isLoggedIn)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `failed login sets error message`() = runTest {
        coEvery { repo.login(any(), any()) } returns Result.failure(Exception("401"))
        vm.onUsernameChange("admin")
        vm.onPasswordChange("wrong")
        vm.login()
        assertEquals("Invalid credentials", vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoggedIn)
    }
}
