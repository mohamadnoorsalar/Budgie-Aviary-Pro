package com.example.feature.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.finance.FinanceAnalyticsHelper
import com.example.core.finance.MonthlyFinanceSummary
import com.example.core.finance.ProfitAndLossReport
import com.example.core.finance.ProfitStatus
import com.example.core.finance.YearlyFinanceSummary
import com.example.data.database.entity.ExpenseCategory
import com.example.data.database.entity.ExpenseEntity
import com.example.data.database.entity.IncomeCategory
import com.example.data.database.entity.IncomeEntity
import com.example.data.repository.AviaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(
    private val repository: AviaryRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0) // 0: Overview/P&L, 1: Expenses, 2: Income, 3: Monthly, 4: Yearly
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedExpenseCategory = MutableStateFlow("ALL")
    val selectedExpenseCategory: StateFlow<String> = _selectedExpenseCategory.asStateFlow()

    private val _selectedIncomeCategory = MutableStateFlow("ALL")
    val selectedIncomeCategory: StateFlow<String> = _selectedIncomeCategory.asStateFlow()

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allIncome: StateFlow<List<IncomeEntity>> = repository.allIncome
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpenses: StateFlow<Double> = repository.totalExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIncome: StateFlow<Double> = repository.totalIncome
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val allPairs = repository.allPairs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChicks = repository.allChicks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredExpenses: StateFlow<List<ExpenseEntity>> = combine(
        allExpenses,
        _selectedExpenseCategory
    ) { expenses, category ->
        if (category == "ALL") expenses else expenses.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredIncome: StateFlow<List<IncomeEntity>> = combine(
        allIncome,
        _selectedIncomeCategory
    ) { incomes, category ->
        if (category == "ALL") incomes else incomes.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Profit and Loss report combining real expense, income, pair count, chick count
    val profitAndLossReport: StateFlow<ProfitAndLossReport> = combine(
        allExpenses,
        allIncome,
        allPairs,
        allChicks
    ) { expenses, incomes, pairs, chicks ->
        FinanceAnalyticsHelper.computeProfitAndLoss(
            expenses = expenses,
            incomes = incomes,
            totalPairsCount = pairs.size,
            totalChicksCount = chicks.size
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ProfitAndLossReport(0.0, 0.0, 0.0, 0.0, ProfitStatus.BREAK_EVEN, emptyMap(), emptyMap(), 0, 0, null, null, null)
    )

    // Monthly Summaries
    val monthlySummaries: StateFlow<List<MonthlyFinanceSummary>> = combine(
        allExpenses,
        allIncome
    ) { expenses, incomes ->
        FinanceAnalyticsHelper.computeMonthlySummaries(expenses, incomes)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Yearly Summaries
    val yearlySummaries: StateFlow<List<YearlyFinanceSummary>> = monthlySummaries.combine(_selectedTab) { monthly, _ ->
        FinanceAnalyticsHelper.computeYearlySummaries(monthly)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun selectExpenseCategory(category: String) {
        _selectedExpenseCategory.value = category
    }

    fun selectIncomeCategory(category: String) {
        _selectedIncomeCategory.value = category
    }

    fun recordExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.saveExpense(expense)
            repository.recordAuditLog(
                actionType = "RECORD_EXPENSE",
                entityType = "FINANCE",
                entityId = expense.id,
                summary = "Recorded expense: ${expense.title} ($${expense.amount}) [${expense.category}]"
            )
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            repository.recordAuditLog(
                actionType = "DELETE_EXPENSE",
                entityType = "FINANCE",
                entityId = expense.id,
                summary = "Deleted expense: ${expense.title} ($${expense.amount})"
            )
        }
    }

    fun recordIncome(income: IncomeEntity) {
        viewModelScope.launch {
            repository.saveIncome(income)
            repository.recordAuditLog(
                actionType = "RECORD_INCOME",
                entityType = "FINANCE",
                entityId = income.id,
                summary = "Recorded income: ${income.title} ($${income.amount}) [${income.category}]"
            )
        }
    }

    fun deleteIncome(income: IncomeEntity) {
        viewModelScope.launch {
            repository.deleteIncome(income)
            repository.recordAuditLog(
                actionType = "DELETE_INCOME",
                entityType = "FINANCE",
                entityId = income.id,
                summary = "Deleted income: ${income.title} ($${income.amount})"
            )
        }
    }
}

class FinanceViewModelFactory(
    private val repository: AviaryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            return FinanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
