package com.example.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insertExpense(expense: Task)

    @Update
    suspend fun updateExpense(expense: Task)

    @Delete
    suspend fun deleteExpense(expense: Task)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllEspenses(): Flow<List<Task>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Task?
}