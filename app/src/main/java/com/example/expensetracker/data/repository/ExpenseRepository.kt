package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.TaskDao
import com.example.expensetracker.data.model.Task

class ExpenseRepository(
    private val taskDao: TaskDao
) {

    suspend fun insertTask(task: Task) {
        return taskDao.insertExpense(task)
    }
    suspend fun updateTask(task: Task) {
        taskDao.updateExpense(task)
    }
    suspend fun deleteTask(task: Task) {
        taskDao.deleteExpense(task)
    }

    fun getAllEspenses() = taskDao.getAllEspenses()

    suspend fun getExpenseById(id: Long) = taskDao.getExpenseById(id)
}