package com.example.feature.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.finance.MonthlyFinanceSummary
import com.example.core.finance.ProfitAndLossReport
import com.example.core.finance.ProfitStatus
import com.example.core.finance.YearlyFinanceSummary
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.ui.components.EmptyStateView
import com.example.core.ui.components.StatMetricCard
import com.example.data.database.AppDatabase
import com.example.data.database.entity.ExpenseCategory
import com.example.data.database.entity.ExpenseEntity
import com.example.data.database.entity.IncomeCategory
import com.example.data.database.entity.IncomeEntity
import com.example.data.repository.AviaryRepositoryImpl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FinanceScreen(
    modifier: Modifier = Modifier,
    viewModel: FinanceViewModel = run {
        val context = LocalContext.current
        val db = AppDatabase.getInstance(context)
        val repo = AviaryRepositoryImpl(db)
        viewModel(factory = FinanceViewModelFactory(repo))
    }
) {
    val currentLang = LocalAppLanguage.current
    val isFa = currentLang == AppLanguage.PERSIAN

    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedExpenseCategory by viewModel.selectedExpenseCategory.collectAsState()
    val selectedIncomeCategory by viewModel.selectedIncomeCategory.collectAsState()

    val allExpenses by viewModel.allExpenses.collectAsState()
    val allIncome by viewModel.allIncome.collectAsState()
    val filteredExpenses by viewModel.filteredExpenses.collectAsState()
    val filteredIncome by viewModel.filteredIncome.collectAsState()
    val pnlReport by viewModel.profitAndLossReport.collectAsState()
    val monthlySummaries by viewModel.monthlySummaries.collectAsState()
    val yearlySummaries by viewModel.yearlySummaries.collectAsState()

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddIncomeDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("finance_screen"),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top High-Level Metrics
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Income, Expense, Profit Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = if (isFa) "مجموع درآمد" else "Total Income",
                        value = "$${"%.2f".format(pnlReport.totalIncome)}",
                        icon = Icons.Filled.TrendingUp,
                        iconTint = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f),
                        testTag = "stat_income"
                    )
                    StatMetricCard(
                        title = if (isFa) "مجموع مخارج" else "Total Expenses",
                        value = "$${"%.2f".format(pnlReport.totalExpenses)}",
                        icon = Icons.Filled.TrendingDown,
                        iconTint = Color(0xFFC62828),
                        modifier = Modifier.weight(1f),
                        testTag = "stat_expense"
                    )
                    StatMetricCard(
                        title = if (isFa) "سود خالص (P&L)" else "Net Profit / Loss",
                        value = "${if (pnlReport.netProfit >= 0) "+" else ""}$${"%.2f".format(pnlReport.netProfit)}",
                        icon = when (pnlReport.status) {
                            ProfitStatus.PROFIT -> Icons.Filled.TrendingUp
                            ProfitStatus.LOSS -> Icons.Filled.TrendingDown
                            ProfitStatus.BREAK_EVEN -> Icons.Filled.TrendingFlat
                        },
                        iconTint = when (pnlReport.status) {
                            ProfitStatus.PROFIT -> Color(0xFF2E7D32)
                            ProfitStatus.LOSS -> Color(0xFFC62828)
                            ProfitStatus.BREAK_EVEN -> Color(0xFF757575)
                        },
                        modifier = Modifier.weight(1.1f),
                        testTag = "stat_net_profit"
                    )
                }

                // Cost per Pair & Cost per Chick Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = if (isFa) "هزینه تمام‌شده هر جفت" else "Cost per Pair",
                        value = pnlReport.costPerPair?.let { "$${"%.2f".format(it)}" } ?: if (isFa) "ثبت نشده" else "N/A",
                        subtitle = if (isFa) "بر اساس ${pnlReport.totalPairs} جفت فعال" else "Based on ${pnlReport.totalPairs} pairs",
                        icon = Icons.Filled.Favorite,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_cost_per_pair"
                    )
                    StatMetricCard(
                        title = if (isFa) "هزینه تمام‌شده هر جوجه" else "Cost per Chick",
                        value = pnlReport.costPerChick?.let { "$${"%.2f".format(it)}" } ?: if (isFa) "ثبت نشده" else "N/A",
                        subtitle = if (isFa) "بر اساس ${pnlReport.totalChicks} جوجه متولد" else "Based on ${pnlReport.totalChicks} chicks",
                        icon = Icons.Filled.Egg,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_cost_per_chick"
                    )
                }

                // Action Bar: Record Expense & Record Income
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showAddExpenseDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("btn_record_expense")
                    ) {
                        Icon(Icons.Filled.TrendingDown, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isFa) "ثبت هزینه (-)" else "Record Expense")
                    }

                    Button(
                        onClick = { showAddIncomeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("btn_record_income")
                    ) {
                        Icon(Icons.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isFa) "ثبت درآمد (+)" else "Record Income")
                    }
                }

                // Tab Selector
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        text = { Text(if (isFa) "گزارش P&L" else "P&L Report") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        text = { Text(if (isFa) "هزینه‌ها (${allExpenses.size})" else "Expenses (${allExpenses.size})") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        text = { Text(if (isFa) "درآمدها (${allIncome.size})" else "Income (${allIncome.size})") }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { viewModel.selectTab(3) },
                        text = { Text(if (isFa) "ماهانه (${monthlySummaries.size})" else "Monthly") }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { viewModel.selectTab(4) },
                        text = { Text(if (isFa) "سالانه (${yearlySummaries.size})" else "Yearly") }
                    )
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Overview & Profit & Loss Statement
                item {
                    ProfitAndLossSection(
                        report = pnlReport,
                        isPersian = isFa
                    )
                }
            }
            1 -> {
                // Expenses Tab
                item {
                    // Expense category filter chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf(
                            "ALL" to (if (isFa) "همه" else "All"),
                            ExpenseCategory.BIRD_PURCHASE to (if (isFa) "خرید پرنده" else "Bird Purchases"),
                            ExpenseCategory.FOOD to (if (isFa) "دان و خوراک" else "Food"),
                            ExpenseCategory.SUPPLEMENTS to (if (isFa) "مکمل‌ها" else "Supplements"),
                            ExpenseCategory.MEDICINE to (if (isFa) "دارو و درمان" else "Medicine"),
                            ExpenseCategory.EQUIPMENT to (if (isFa) "تجهیزات" else "Equipment"),
                            ExpenseCategory.OTHER to (if (isFa) "سایر هزینه‌ها" else "Other")
                        )
                        items(categories) { (catKey, catLabel) ->
                            FilterChip(
                                selected = selectedExpenseCategory == catKey,
                                onClick = { viewModel.selectExpenseCategory(catKey) },
                                label = { Text(catLabel) }
                            )
                        }
                    }
                }

                if (filteredExpenses.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Filled.TrendingDown,
                            title = if (isFa) "هیچ هزینه‌ای ثبت نشده است" else "No Expenses Recorded",
                            description = if (isFa) "هزینه‌های خرید دان، مکمل، قفس و دارو را ثبت کنید." else "Record outlays for seed, medication, cage equipment, and bird purchases.",
                            actionLabel = if (isFa) "ثبت اولین هزینه" else "Record First Expense",
                            onActionClick = { showAddExpenseDialog = true },
                            testTag = "empty_expenses_view"
                        )
                    }
                } else {
                    items(filteredExpenses, key = { it.id }) { expense ->
                        ExpenseItemCard(
                            expense = expense,
                            dateFormat = dateFormat,
                            isPersian = isFa,
                            onDelete = { viewModel.deleteExpense(expense) }
                        )
                    }
                }
            }
            2 -> {
                // Income Tab
                item {
                    // Income category filter chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf(
                            "ALL" to (if (isFa) "همه" else "All"),
                            IncomeCategory.BIRD_SALE to (if (isFa) "فروش پرنده بالغ" else "Bird Sales"),
                            IncomeCategory.CHICK_SALE to (if (isFa) "فروش جوجه" else "Chick Sales"),
                            IncomeCategory.OTHER to (if (isFa) "سایر درآمدها" else "Other Income")
                        )
                        items(categories) { (catKey, catLabel) ->
                            FilterChip(
                                selected = selectedIncomeCategory == catKey,
                                onClick = { viewModel.selectIncomeCategory(catKey) },
                                label = { Text(catLabel) }
                            )
                        }
                    }
                }

                if (filteredIncome.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Filled.TrendingUp,
                            title = if (isFa) "هیچ دریافتی و درآمدی ثبت نشده است" else "No Income Recorded",
                            description = if (isFa) "درآمد حاصل از فروش پرنده‌ها و جوجه‌ها را ثبت کنید." else "Record revenues from budgie sales, chicks, or competition awards.",
                            actionLabel = if (isFa) "ثبت اولین درآمد" else "Record First Income",
                            onActionClick = { showAddIncomeDialog = true },
                            testTag = "empty_income_view"
                        )
                    }
                } else {
                    items(filteredIncome, key = { it.id }) { income ->
                        IncomeItemCard(
                            income = income,
                            dateFormat = dateFormat,
                            isPersian = isFa,
                            onDelete = { viewModel.deleteIncome(income) }
                        )
                    }
                }
            }
            3 -> {
                // Monthly Summary
                if (monthlySummaries.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Filled.CalendarMonth,
                            title = if (isFa) "داده‌های مالی ماهانه وجود ندارد" else "No Monthly Data Available",
                            description = if (isFa) "با ثبت تراکنش‌های مالی، خلاصه درآمد، مخارج و سود ماهانه تولید می‌شود." else "Monthly summaries will generate automatically when you log transactions.",
                            actionLabel = if (isFa) "ثبت هزینه" else "Add Expense",
                            onActionClick = { showAddExpenseDialog = true }
                        )
                    }
                } else {
                    items(monthlySummaries, key = { it.yearMonth }) { summary ->
                        MonthlySummaryCard(
                            summary = summary,
                            isPersian = isFa
                        )
                    }
                }
            }
            4 -> {
                // Yearly Summary
                if (yearlySummaries.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Filled.Assessment,
                            title = if (isFa) "داده‌های مالی سالانه وجود ندارد" else "No Yearly Data Available",
                            description = if (isFa) "خلاصه مالی و بیلان سالانه به صورت خودکار محاسبه خواهد شد." else "Yearly P&L and financial trends will appear once transactions are logged.",
                            actionLabel = if (isFa) "ثبت درآمد" else "Add Income",
                            onActionClick = { showAddIncomeDialog = true }
                        )
                    }
                } else {
                    items(yearlySummaries, key = { it.year }) { summary ->
                        YearlySummaryCard(
                            summary = summary,
                            isPersian = isFa
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            isPersian = isFa,
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { expense ->
                viewModel.recordExpense(expense)
                showAddExpenseDialog = false
            }
        )
    }

    if (showAddIncomeDialog) {
        AddIncomeDialog(
            isPersian = isFa,
            onDismiss = { showAddIncomeDialog = false },
            onConfirm = { income ->
                viewModel.recordIncome(income)
                showAddIncomeDialog = false
            }
        )
    }
}

@Composable
fun ProfitAndLossSection(
    report: ProfitAndLossReport,
    isPersian: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Detailed P&L Statement Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("pnl_statement_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isPersian) "صورت سود و زیان (P&L Statement)" else "Profit & Loss Statement",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (isPersian) "کل درآمد سالن (+)" else "Total Gross Revenue (+)", style = MaterialTheme.typography.bodyMedium)
                    Text("$${"%.2f".format(report.totalIncome)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E7D32))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (isPersian) "کل هزینه‌های عملیاتی (-)" else "Total Operating Expenses (-)", style = MaterialTheme.typography.bodyMedium)
                    Text("$${"%.2f".format(report.totalExpenses)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFC62828))
                }

                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isPersian) "سود / زیان خالص" else "Net Operating Profit / Loss",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isPersian) "حاشیه سود: ${"%.1f".format(report.profitMarginPercentage)}%"
                            else "Profit Margin: ${"%.1f".format(report.profitMarginPercentage)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "${if (report.netProfit >= 0) "+" else ""}$${"%.2f".format(report.netProfit)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = when (report.status) {
                            ProfitStatus.PROFIT -> Color(0xFF2E7D32)
                            ProfitStatus.LOSS -> Color(0xFFC62828)
                            ProfitStatus.BREAK_EVEN -> Color(0xFF757575)
                        }
                    )
                }
            }
        }

        // Expense Breakdown by Category
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isPersian) "تفکیک هزینه‌ها بر اساس سرفصل" else "Expense Category Breakdown",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                val catLabels = mapOf(
                    ExpenseCategory.BIRD_PURCHASE to (if (isPersian) "خرید پرنده" else "Bird Purchases"),
                    ExpenseCategory.FOOD to (if (isPersian) "دان و خوراک" else "Food Costs"),
                    ExpenseCategory.SUPPLEMENTS to (if (isPersian) "مکمل‌ها و ویتامین" else "Supplement Costs"),
                    ExpenseCategory.MEDICINE to (if (isPersian) "دارو و درمان" else "Medicine Costs"),
                    ExpenseCategory.EQUIPMENT to (if (isPersian) "تجهیزات و قفس" else "Equipment Costs"),
                    ExpenseCategory.OTHER to (if (isPersian) "سایر هزینه‌ها" else "Other Expenses")
                )

                ExpenseCategory.ALL.forEach { cat ->
                    val amt = report.expenseByCategory[cat] ?: 0.0
                    val pct = if (report.totalExpenses > 0) (amt / report.totalExpenses) * 100 else 0.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${catLabels[cat] ?: cat} (${"%.0f".format(pct)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${"%.2f".format(amt)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Income Breakdown by Category
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isPersian) "تفکیک درآمدها بر اساس سرفصل" else "Income Category Breakdown",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                val incLabels = mapOf(
                    IncomeCategory.BIRD_SALE to (if (isPersian) "فروش پرنده بالغ" else "Bird Sales"),
                    IncomeCategory.CHICK_SALE to (if (isPersian) "فروش جوجه" else "Chick Sales"),
                    IncomeCategory.OTHER to (if (isPersian) "سایر درآمدها" else "Other Income")
                )

                IncomeCategory.ALL.forEach { cat ->
                    val amt = report.incomeByCategory[cat] ?: 0.0
                    val pct = if (report.totalIncome > 0) (amt / report.totalIncome) * 100 else 0.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${incLabels[cat] ?: cat} (${"%.0f".format(pct)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${"%.2f".format(amt)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseItemCard(
    expense: ExpenseEntity,
    dateFormat: SimpleDateFormat,
    isPersian: Boolean,
    onDelete: () -> Unit
) {
    val catLabel = when (expense.category) {
        ExpenseCategory.BIRD_PURCHASE -> if (isPersian) "خرید پرنده" else "Bird Purchase"
        ExpenseCategory.FOOD -> if (isPersian) "دان و خوراک" else "Food"
        ExpenseCategory.SUPPLEMENTS -> if (isPersian) "مکمل" else "Supplements"
        ExpenseCategory.MEDICINE -> if (isPersian) "دارو" else "Medicine"
        ExpenseCategory.EQUIPMENT -> if (isPersian) "تجهیزات" else "Equipment"
        else -> if (isPersian) "سایر" else "Other"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("expense_item_${expense.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFC62828).copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.TrendingDown,
                    contentDescription = null,
                    tint = Color(0xFFC62828),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = expense.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "-$${"%.2f".format(expense.amount)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFC62828)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$catLabel • ${dateFormat.format(Date(expense.date))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!expense.pairId.isNullOrBlank()) {
                        Text(
                            text = "Pair: ${expense.pairId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (!expense.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = expense.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp).testTag("btn_delete_expense_${expense.id}")
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun IncomeItemCard(
    income: IncomeEntity,
    dateFormat: SimpleDateFormat,
    isPersian: Boolean,
    onDelete: () -> Unit
) {
    val catLabel = when (income.category) {
        IncomeCategory.BIRD_SALE -> if (isPersian) "فروش پرنده" else "Bird Sale"
        IncomeCategory.CHICK_SALE -> if (isPersian) "فروش جوجه" else "Chick Sale"
        else -> if (isPersian) "سایر درآمد" else "Other Income"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("income_item_${income.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF2E7D32).copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = income.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "+$${"%.2f".format(income.amount)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2E7D32)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$catLabel • ${dateFormat.format(Date(income.date))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!income.soldBirdRingNumber.isNullOrBlank()) {
                        Text(
                            text = "Ring: ${income.soldBirdRingNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (!income.buyerName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Buyer: ${income.buyerName} ${income.buyerContact?.let { "($it)" } ?: ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp).testTag("btn_delete_income_${income.id}")
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun MonthlySummaryCard(
    summary: MonthlyFinanceSummary,
    isPersian: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("monthly_summary_${summary.yearMonth}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.displayLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = (if (summary.netProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${if (summary.netProfit >= 0) "+" else ""}$${"%.2f".format(summary.netProfit)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (summary.netProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(if (isPersian) "درآمد ماه" else "Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("+$${"%.2f".format(summary.totalIncome)}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E7D32))
                }
                Column {
                    Text(if (isPersian) "مخارج ماه" else "Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("-$${"%.2f".format(summary.totalExpenses)}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFC62828))
                }
                Column {
                    Text(if (isPersian) "سود خالص" else "Net P&L", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${if (summary.netProfit >= 0) "+" else ""}$${"%.2f".format(summary.netProfit)}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun YearlySummaryCard(
    summary: YearlyFinanceSummary,
    isPersian: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("yearly_summary_${summary.year}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${summary.year}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = (if (summary.netProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${if (summary.netProfit >= 0) "+" else ""}$${"%.2f".format(summary.netProfit)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (summary.netProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(if (isPersian) "درآمد کل سال" else "Annual Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("+$${"%.2f".format(summary.totalIncome)}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E7D32))
                }
                Column {
                    Text(if (isPersian) "مخارج کل سال" else "Annual Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("-$${"%.2f".format(summary.totalExpenses)}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFC62828))
                }
                Column {
                    Text(if (isPersian) "ماه‌های فعال" else "Active Months", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${summary.monthlySummaries.size}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
