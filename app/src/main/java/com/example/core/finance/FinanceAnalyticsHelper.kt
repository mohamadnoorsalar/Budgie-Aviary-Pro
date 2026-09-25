package com.example.core.finance

import com.example.data.database.entity.ExpenseCategory
import com.example.data.database.entity.ExpenseEntity
import com.example.data.database.entity.IncomeCategory
import com.example.data.database.entity.IncomeEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthlyFinanceSummary(
    val yearMonth: String, // e.g. "2026-09"
    val displayLabel: String, // e.g. "Sep 2026"
    val year: Int,
    val month: Int, // 1-12
    val totalIncome: Double,
    val totalExpenses: Double,
    val netProfit: Double,
    val expenseBreakdown: Map<String, Double>,
    val incomeBreakdown: Map<String, Double>
)

data class YearlyFinanceSummary(
    val year: Int,
    val totalIncome: Double,
    val totalExpenses: Double,
    val netProfit: Double,
    val monthlySummaries: List<MonthlyFinanceSummary>
)

enum class ProfitStatus {
    PROFIT,
    LOSS,
    BREAK_EVEN
}

data class ProfitAndLossReport(
    val totalIncome: Double,
    val totalExpenses: Double,
    val netProfit: Double,
    val profitMarginPercentage: Double,
    val status: ProfitStatus,
    val expenseByCategory: Map<String, Double>,
    val incomeByCategory: Map<String, Double>,
    val totalPairs: Int,
    val totalChicks: Int,
    val costPerPair: Double?,
    val costPerChick: Double?,
    val revenuePerChickSold: Double?
)

object FinanceAnalyticsHelper {

    private val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    private val displayMonthFormat = SimpleDateFormat("MMM yyyy", Locale.US)

    /**
     * Computes the complete Profit and Loss report from raw database records.
     */
    fun computeProfitAndLoss(
        expenses: List<ExpenseEntity>,
        incomes: List<IncomeEntity>,
        totalPairsCount: Int = 0,
        totalChicksCount: Int = 0
    ): ProfitAndLossReport {
        val validExpenses = expenses.filter { !it.isDeleted }
        val validIncomes = incomes.filter { !it.isDeleted }

        val totalExpenses = validExpenses.sumOf { it.amount }
        val totalIncome = validIncomes.sumOf { it.amount }
        val netProfit = totalIncome - totalExpenses

        val profitMargin = if (totalIncome > 0.0) {
            (netProfit / totalIncome) * 100.0
        } else if (totalExpenses > 0.0) {
            -100.0
        } else {
            0.0
        }

        val status = when {
            netProfit > 0.001 -> ProfitStatus.PROFIT
            netProfit < -0.001 -> ProfitStatus.LOSS
            else -> ProfitStatus.BREAK_EVEN
        }

        // Expense by Category
        val expenseByCategory = mutableMapOf<String, Double>()
        ExpenseCategory.ALL.forEach { expenseByCategory[it] = 0.0 }
        validExpenses.forEach { exp ->
            val cat = if (ExpenseCategory.ALL.contains(exp.category)) exp.category else ExpenseCategory.OTHER
            expenseByCategory[cat] = (expenseByCategory[cat] ?: 0.0) + exp.amount
        }

        // Income by Category
        val incomeByCategory = mutableMapOf<String, Double>()
        IncomeCategory.ALL.forEach { incomeByCategory[it] = 0.0 }
        validIncomes.forEach { inc ->
            val cat = if (IncomeCategory.ALL.contains(inc.category)) inc.category else IncomeCategory.OTHER
            incomeByCategory[cat] = (incomeByCategory[cat] ?: 0.0) + inc.amount
        }

        // Breeding Specific Costs: Food, Supplements, Medicine, and Direct Pair costs
        val breedingExpenses = validExpenses.filter {
            it.category == ExpenseCategory.FOOD ||
                    it.category == ExpenseCategory.SUPPLEMENTS ||
                    it.category == ExpenseCategory.MEDICINE ||
                    it.pairId != null
        }.sumOf { it.amount }

        // Use overall total expenses for aviary cost per unit, or breeding expenses if available
        val effectiveBreedingCost = if (breedingExpenses > 0) breedingExpenses else totalExpenses

        val costPerPair = if (totalPairsCount > 0) {
            effectiveBreedingCost / totalPairsCount.toDouble()
        } else null

        val costPerChick = if (totalChicksCount > 0) {
            effectiveBreedingCost / totalChicksCount.toDouble()
        } else null

        val chickSales = validIncomes.filter { it.category == IncomeCategory.CHICK_SALE }
        val totalChickRevenue = chickSales.sumOf { it.amount }
        val revenuePerChickSold = if (chickSales.isNotEmpty()) {
            totalChickRevenue / chickSales.size.toDouble()
        } else null

        return ProfitAndLossReport(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netProfit = netProfit,
            profitMarginPercentage = profitMargin,
            status = status,
            expenseByCategory = expenseByCategory,
            incomeByCategory = incomeByCategory,
            totalPairs = totalPairsCount,
            totalChicks = totalChicksCount,
            costPerPair = costPerPair,
            costPerChick = costPerChick,
            revenuePerChickSold = revenuePerChickSold
        )
    }

    /**
     * Groups expenses and income by month into chronological Monthly summaries.
     */
    fun computeMonthlySummaries(
        expenses: List<ExpenseEntity>,
        incomes: List<IncomeEntity>
    ): List<MonthlyFinanceSummary> {
        val validExpenses = expenses.filter { !it.isDeleted }
        val validIncomes = incomes.filter { !it.isDeleted }

        val allTimestamps = (validExpenses.map { it.date } + validIncomes.map { it.date }).distinct()
        if (allTimestamps.isEmpty()) return emptyList()

        val groupedExpenses = validExpenses.groupBy { yearMonthFormat.format(Date(it.date)) }
        val groupedIncomes = validIncomes.groupBy { yearMonthFormat.format(Date(it.date)) }

        val allYearMonths = (groupedExpenses.keys + groupedIncomes.keys).distinct().sortedDescending()

        val calendar = Calendar.getInstance()

        return allYearMonths.map { ym ->
            val monthExpenses = groupedExpenses[ym] ?: emptyList()
            val monthIncomes = groupedIncomes[ym] ?: emptyList()

            val expTotal = monthExpenses.sumOf { it.amount }
            val incTotal = monthIncomes.sumOf { it.amount }

            val expBreakdown = mutableMapOf<String, Double>()
            ExpenseCategory.ALL.forEach { expBreakdown[it] = 0.0 }
            monthExpenses.forEach { exp ->
                val cat = if (ExpenseCategory.ALL.contains(exp.category)) exp.category else ExpenseCategory.OTHER
                expBreakdown[cat] = (expBreakdown[cat] ?: 0.0) + exp.amount
            }

            val incBreakdown = mutableMapOf<String, Double>()
            IncomeCategory.ALL.forEach { incBreakdown[it] = 0.0 }
            monthIncomes.forEach { inc ->
                val cat = if (IncomeCategory.ALL.contains(inc.category)) inc.category else IncomeCategory.OTHER
                incBreakdown[cat] = (incBreakdown[cat] ?: 0.0) + inc.amount
            }

            // Parse year and month
            val date = try { yearMonthFormat.parse(ym) } catch (_: Exception) { null }
            val (year, month, displayLabel) = if (date != null) {
                calendar.time = date
                Triple(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    displayMonthFormat.format(date)
                )
            } else {
                Triple(2026, 1, ym)
            }

            MonthlyFinanceSummary(
                yearMonth = ym,
                displayLabel = displayLabel,
                year = year,
                month = month,
                totalIncome = incTotal,
                totalExpenses = expTotal,
                netProfit = incTotal - expTotal,
                expenseBreakdown = expBreakdown,
                incomeBreakdown = incBreakdown
            )
        }
    }

    /**
     * Groups monthly summaries into yearly summaries.
     */
    fun computeYearlySummaries(
        monthlySummaries: List<MonthlyFinanceSummary>
    ): List<YearlyFinanceSummary> {
        return monthlySummaries.groupBy { it.year }.map { (year, months) ->
            val totalInc = months.sumOf { it.totalIncome }
            val totalExp = months.sumOf { it.totalExpenses }
            YearlyFinanceSummary(
                year = year,
                totalIncome = totalInc,
                totalExpenses = totalExp,
                netProfit = totalInc - totalExp,
                monthlySummaries = months.sortedBy { it.month }
            )
        }.sortedByDescending { it.year }
    }
}
