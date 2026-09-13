package com.example.expensetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "expenses")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val description: String,
    val amount: Double = 0.0,
    val date: Long = System.currentTimeMillis()
) {
    val price: Double get() = amount
}