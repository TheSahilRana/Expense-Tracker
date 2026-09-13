package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.room.Room
import com.example.expensetracker.data.local.ExpenseDatabase
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.navigation.NavGraph
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.viewmodel.AddEditViewModelFactory
import com.example.expensetracker.viewmodel.HomeScreenViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = Room.databaseBuilder(
            context = applicationContext,
            ExpenseDatabase::class.java,
            "studysync.db"
        ).fallbackToDestructiveMigration()
        .build()
        val taskDao = database.taskDao()
        val repository = ExpenseRepository(taskDao)
        val homeFactory = HomeScreenViewModelFactory(application, repository)
        val addeditFactory = AddEditViewModelFactory(application, repository)
        setContent {
            ExpenseTrackerTheme {
                NavGraph(homeFactory,addeditFactory)
            }
        }
    }
}
