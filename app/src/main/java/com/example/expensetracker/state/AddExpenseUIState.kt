package com.example.expensetracker.state

data class AddExpenseUIState(
    val expenseTitle: String = "",
    val expenseCategory: String = "",
    val expenseDescription: String = "",
    val expenseAmount: String = "",
    val expenseDate: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)
