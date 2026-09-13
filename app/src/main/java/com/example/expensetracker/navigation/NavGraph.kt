package com.example.expensetracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.expensetracker.screens.AddEditExpenseScreen
import com.example.expensetracker.screens.HomeScreen
import com.example.expensetracker.viewmodel.AddEditViewModelFactory
import com.example.expensetracker.viewmodel.HomeScreenViewModelFactory

@Composable
fun NavGraph(homeFactory: HomeScreenViewModelFactory,addeditFactory: AddEditViewModelFactory) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Home) {
        composable<Routes.Home> {
            HomeScreen(
                homeFactory,
                onAddExpenseClick = {
                    navController.navigate(Routes.AddExpense)
                },
                onEditExpenseClick = { taskId ->
                    navController.navigate(Routes.EditExpense(taskId = taskId))
                }
            )
        }
        composable<Routes.AddExpense> {
            AddEditExpenseScreen(
                addeditFactory,
                navController = navController,
                taskId = null
            )
        }
        composable<Routes.EditExpense> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.EditExpense>()
            AddEditExpenseScreen(
                addeditFactory,
                navController = navController,
                taskId = route.taskId
            )
        }
    }
}