package com.example.expensetracker.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.data.model.Task
import com.example.expensetracker.viewmodel.HomeScreenViewModel
import com.example.expensetracker.viewmodel.HomeScreenViewModelFactory
import com.example.studysync.utils.formatDate
import java.util.Locale
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.Instant


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    factory: HomeScreenViewModelFactory,
    onAddExpenseClick: () -> Unit = {},
    onEditExpenseClick: (Long) -> Unit = {}
) {

    val viewModel: HomeScreenViewModel = viewModel(
        factory = factory
    )

    val expenses by viewModel.expenses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val periods = listOf("All","Today", "Week", "Month")
    var selectedPeriod by remember { mutableStateOf("All") }


    val filteredExpenses = expenses.filter { expense->
        val matchesSearch = expense.title.contains(searchQuery, ignoreCase = true) ||
                expense.category.contains(searchQuery, ignoreCase = true)
        val matchesPeriod = selectedPeriod == "All" || check(expense,selectedPeriod)
        matchesSearch && matchesPeriod
    }

    val groupedExpenses = remember(filteredExpenses) { groupExpensesByMonth(filteredExpenses) }

    val totalAmount = filteredExpenses.sumOf { it.amount }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Expenses",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpenseClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Header,
            item {
                Column {
                    Text(
                        text = "Welcome back! 👋",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Track and manage your expenses",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Summary Card
            item {
                TotalExpenseCard(
                    totalAmount = String.format(Locale.US, "₹%.2f", totalAmount),
                    period = selectedPeriod
                )
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Search transactions...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search Icon")
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = searchQuery.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            // Time Period Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    periods.forEach { period ->
                        val isSelected = period == selectedPeriod
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPeriod = period },
                            label = {
                                Text(
                                    text = period,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // List Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${filteredExpenses.size} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty State
            if (filteredExpenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No expenses found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                groupedExpenses.forEach { (monthName, data) ->
                    val (totalAmount, monthItems) = data

                    // 🌟 THE MONTH HEADER (e.g., [September 1200])
                    stickyHeader {
                        MonthSummary(monthName = monthName, totalAmount = totalAmount)
                    }

                    // 💸 THE EXPENSE ROWS FOR THIS MONTH
                    items(monthItems, key = { it.id }) { expense ->
                        SpendItem(
                            expense = expense,
                            onEdit = { onEditExpenseClick(expense.id) },
                            onDelete = { viewModel.deleteTask(expense) }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun MonthSummary(monthName: String, totalAmount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = monthName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = String.format(Locale.US, "₹%.2f", totalAmount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TotalExpenseCard(totalAmount: String, period: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Total Spent ($period)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = totalAmount,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun SpendItem(
    expense: Task,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
//            Box(
//                modifier = Modifier
//                    .size(44.dp)
//                    .clip(CircleShape)
//                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = expense.icon,
//                    contentDescription = expense.category,
//                    tint = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.size(22.dp)
//                )
//            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${expense.category} • ${formatDate(expense.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount
            Text(
                text = String.format(Locale.US, "-₹%.2f", expense.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Quick Actions
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit ${expense.title}",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = {showDialog=true},
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete ${expense.title}",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                    },
                    title = {
                        Text(text = "Confirm Deletion")
                    },
                    text = {
                        Text(text = "Are you sure you want to permanently delete this item? This action cannot be undone.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDialog = false
                                onDelete()
                            }
                        ) {
                            Text(text = "Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDialog = false
                            }
                        ) {
                            Text(text = "Cancel")
                        }
                    }
                )
            }

        }
    }
}

fun check(expense: Task , period: String): Boolean {
    val userZone = ZoneId.systemDefault()
    val today = LocalDate.now(userZone)

//  TODAY
    val startOfToday = today.atStartOfDay(userZone).toInstant().toEpochMilli()
    val endOfToday = today.plusDays(1).atStartOfDay(userZone).toInstant().toEpochMilli()

//  THIS WEEK (Monday to Sunday)
//    val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
//        .atStartOfDay(userZone).toInstant().toEpochMilli()
//
//    val endOfWeek = today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
//        .plusDays(1) // Moves to next Monday 00:00:00 to catch everything on Sunday
//        .atStartOfDay(userZone).toInstant().toEpochMilli()
    val startOfWeek = today
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .atStartOfDay(userZone)
        .toInstant()
        .toEpochMilli()

    val endOfWeek = startOfWeek + 7 * 24 * 60 * 60 * 1000

//  THIS MONTH (1st to Last Day)
    val startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth())
        .atStartOfDay(userZone).toInstant().toEpochMilli()

    val endOfMonth = today.with(TemporalAdjusters.firstDayOfNextMonth())
        .atStartOfDay(userZone).toInstant().toEpochMilli()

    if(period=="Today" && expense.date >= startOfToday && expense.date < endOfToday) return true
    if(period=="Week" && expense.date >= startOfWeek && expense.date < endOfWeek) return true
    if(period=="Month" && expense.date >= startOfMonth && expense.date < endOfMonth) return true
    return false
}

fun groupExpensesByMonth(expenses: List<Task>): Map<String, Pair<Double, List<Task>>> {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    val userZone = ZoneId.systemDefault()

    // 1. Group the flat list by month name string
    val groupedByMonthName = expenses.groupBy { expense ->
        Instant.ofEpochMilli(expense.date)
            .atZone(userZone)
            .format(formatter)
    }

    // 2. Map over the groups to calculate the total sum for each month
    return groupedByMonthName.mapValues { (_, items) ->
        val totalSum = items.sumOf { it.amount }
        Pair(totalSum, items)
    }
}