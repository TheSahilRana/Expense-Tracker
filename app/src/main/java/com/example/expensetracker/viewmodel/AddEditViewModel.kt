package com.example.expensetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.model.Task
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.state.AddExpenseUIState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed class AddTaskEvent {
    object TaskSaved : AddTaskEvent()
    object TaskUpdated : AddTaskEvent()
}

class AddTaskViewModel(
    application: Application,
    private val repository: ExpenseRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AddExpenseUIState())
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<AddTaskEvent>()
    val event = _event.receiveAsFlow()

    fun onExpenseTitleChange(newTitle: String) {
        _uiState.value = _uiState.value.copy(
            expenseTitle = newTitle,
            errorMessage = null
        )
    }

    fun onExpenseCategoryChange(newCategory: String) {
        _uiState.value = _uiState.value.copy(
            expenseCategory = newCategory,
            errorMessage = null
        )
    }

    fun onExpenseDescriptionChange(newDescription: String) {
        _uiState.value = _uiState.value.copy(
            expenseDescription = newDescription
        )
    }

    fun onExpenseAmountChange(newAmount: String) {
        // Only allow valid numeric / decimal input
        val filtered = newAmount.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            _uiState.value = _uiState.value.copy(
                expenseAmount = filtered,
                errorMessage = null
            )
        }
    }

    fun onExpenseDateChange(newDate: Long) {
        _uiState.value = _uiState.value.copy(
            expenseDate = newDate
        )
    }

    private fun validateTask(state: AddExpenseUIState): Boolean {
        if (state.expenseTitle.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Expense title is required.")
            return false
        }
        val amount = state.expenseAmount.toDoubleOrNull()
        if (state.expenseAmount.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please enter price in rupees (₹).")
            return false
        }
        if (amount == null || amount <= 0.0) {
            _uiState.value = state.copy(errorMessage = "Please enter a valid price in rupees.")
            return false
        }
        if (state.expenseCategory.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please select or enter a category.")
            return false
        }
        return true
    }

    fun saveTask() {
        val state = uiState.value
        if (!validateTask(state)) {
            return
        }
        val parsedAmount = state.expenseAmount.toDoubleOrNull() ?: 0.0
        val task = Task(
            id = 0,
            title = state.expenseTitle.trim(),
            category = state.expenseCategory.trim(),
            description = state.expenseDescription.trim(),
            amount = parsedAmount,
            date = state.expenseDate
        )
        viewModelScope.launch {
            repository.insertTask(task)
            _event.send(AddTaskEvent.TaskSaved)
        }
    }

    fun loadTask(taskId: Long) {
        viewModelScope.launch {
            val task = repository.getExpenseById(taskId)
            if (task != null) {
                _uiState.value = _uiState.value.copy(
                    expenseTitle = task.title,
                    expenseCategory = task.category,
                    expenseDescription = task.description,
                    expenseAmount = if (task.amount > 0.0) {
                        if (task.amount % 1.0 == 0.0) task.amount.toLong().toString() else task.amount.toString()
                    } else "",
                    expenseDate = if (task.date > 0L) task.date else System.currentTimeMillis(),
                    errorMessage = null
                )
            }
        }
    }

    fun updateTask(taskId: Long) {
        val state = uiState.value
        if (!validateTask(state)) {
            return
        }
        val parsedAmount = state.expenseAmount.toDoubleOrNull() ?: 0.0
        val task = Task(
            id = taskId,
            title = state.expenseTitle.trim(),
            category = state.expenseCategory.trim(),
            description = state.expenseDescription.trim(),
            amount = parsedAmount,
            date = state.expenseDate
        )
        viewModelScope.launch {
            repository.updateTask(task)
            _event.send(AddTaskEvent.TaskUpdated)
        }
    }
}