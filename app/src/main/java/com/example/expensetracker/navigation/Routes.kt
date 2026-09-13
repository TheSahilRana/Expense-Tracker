package com.example.expensetracker.navigation

import kotlinx.serialization.Serializable

class Routes {
    @Serializable
    object Home

    @Serializable
    object AddExpense

    @Serializable
    data class EditExpense(
        val taskId: Long
    )
}