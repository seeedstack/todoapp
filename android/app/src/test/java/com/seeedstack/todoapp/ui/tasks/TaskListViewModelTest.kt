package com.seeedstack.todoapp.ui.tasks

import com.seeedstack.todoapp.data.api.models.TaskDto
import com.seeedstack.todoapp.data.repository.TaskRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: TaskRepository
    private lateinit var vm: TaskListViewModel

    private fun makeTask(id: String, status: String = "pending", priority: String = "medium") =
        TaskDto(id, "Task $id", null, priority, status, "2026-01-01T00:00:00", null, null)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk()
        coEvery { repo.getTasks() } returns Result.success(emptyList())
        vm = TaskListViewModel(repo)
    }

    @After
    fun teardown() { Dispatchers.resetMain() }

    @Test
    fun `loadTasks populates tasks list`() = runTest {
        val tasks = listOf(makeTask("1"), makeTask("2"))
        coEvery { repo.getTasks() } returns Result.success(tasks)
        vm.loadTasks()
        assertEquals(2, vm.uiState.value.tasks.size)
    }

    @Test
    fun `deleteTask removes task from list`() = runTest {
        coEvery { repo.getTasks() } returns Result.success(listOf(makeTask("1"), makeTask("2")))
        coEvery { repo.deleteTask("1") } returns Result.success(Unit)
        vm.loadTasks()
        vm.deleteTask("1")
        assertEquals(1, vm.uiState.value.tasks.size)
        assertEquals("2", vm.uiState.value.tasks.first().id)
    }

    @Test
    fun `filter PENDING shows only pending tasks`() = runTest {
        coEvery { repo.getTasks() } returns Result.success(
            listOf(makeTask("1", "pending"), makeTask("2", "completed"))
        )
        vm.loadTasks()
        vm.setFilter(TaskFilter.PENDING)
        assertEquals(1, vm.uiState.value.filtered.size)
        assertEquals("pending", vm.uiState.value.filtered.first().status)
    }

    @Test
    fun `failed load sets error`() = runTest {
        coEvery { repo.getTasks() } returns Result.failure(Exception("network"))
        vm.loadTasks()
        assertTrue(vm.uiState.value.error != null)
    }
}
