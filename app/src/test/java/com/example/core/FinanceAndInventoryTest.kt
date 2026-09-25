package com.example.core

import com.example.core.finance.FinanceAnalyticsHelper
import com.example.core.finance.ProfitStatus
import com.example.core.inventory.InventoryAnalyticsHelper
import com.example.core.inventory.StockStatus
import com.example.data.database.entity.ExpenseCategory
import com.example.data.database.entity.ExpenseEntity
import com.example.data.database.entity.IncomeCategory
import com.example.data.database.entity.IncomeEntity
import com.example.data.database.entity.InventoryCategory
import com.example.data.database.entity.InventoryItemEntity
import com.example.data.database.entity.StockTransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinanceAndInventoryTest {

    // =========================================================================
    // INVENTORY MODULE TESTS
    // =========================================================================

    @Test
    fun testStockStatusEvaluation() {
        val itemOptimal = InventoryItemEntity(
            sku = "SKU-001",
            name = "Millet Seed",
            category = InventoryCategory.FOOD,
            currentStock = 15.0,
            minStockThreshold = 5.0
        )
        assertEquals(StockStatus.OPTIMAL, InventoryAnalyticsHelper.getStockStatus(itemOptimal))
        assertFalse(InventoryAnalyticsHelper.isLowStock(itemOptimal))

        val itemLowStock = InventoryItemEntity(
            sku = "SKU-002",
            name = "Calcivet Supplement",
            category = InventoryCategory.SUPPLEMENTS,
            currentStock = 3.5,
            minStockThreshold = 5.0
        )
        assertEquals(StockStatus.LOW_STOCK, InventoryAnalyticsHelper.getStockStatus(itemLowStock))
        assertTrue(InventoryAnalyticsHelper.isLowStock(itemLowStock))

        val itemOutOfStock = InventoryItemEntity(
            sku = "SKU-003",
            name = "Antibiotic Drops",
            category = InventoryCategory.MEDICINE,
            currentStock = 0.0,
            minStockThreshold = 2.0
        )
        assertEquals(StockStatus.OUT_OF_STOCK, InventoryAnalyticsHelper.getStockStatus(itemOutOfStock))
        assertTrue(InventoryAnalyticsHelper.isLowStock(itemOutOfStock))
    }

    @Test
    fun testInventorySummaryCalculation() {
        val items = listOf(
            InventoryItemEntity(sku = "1", name = "Seed 1", category = InventoryCategory.FOOD, currentStock = 10.0, minStockThreshold = 2.0, costPerUnit = 2.5),
            InventoryItemEntity(sku = "2", name = "Seed 2", category = InventoryCategory.FOOD, currentStock = 1.0, minStockThreshold = 5.0, costPerUnit = 4.0),
            InventoryItemEntity(sku = "3", name = "Nest Box", category = InventoryCategory.EQUIPMENT, currentStock = 0.0, minStockThreshold = 1.0, costPerUnit = 12.0),
            InventoryItemEntity(sku = "4", name = "Disinfectant", category = InventoryCategory.CONSUMABLES, currentStock = 8.0, minStockThreshold = 2.0, costPerUnit = 5.0),
            InventoryItemEntity(sku = "5", name = "Deleted Item", category = InventoryCategory.FOOD, currentStock = 20.0, minStockThreshold = 2.0, costPerUnit = 1.0, isDeleted = true)
        )

        val summary = InventoryAnalyticsHelper.computeSummary(items)

        assertEquals(4, summary.totalItemsCount) // Excludes deleted item
        assertEquals(1, summary.lowStockCount) // Item 2 has 1.0 <= 5.0
        assertEquals(1, summary.outOfStockCount) // Item 3 has 0.0
        // Expected value: (10.0 * 2.5) + (1.0 * 4.0) + (0.0 * 12.0) + (8.0 * 5.0) = 25 + 4 + 0 + 40 = 69.0
        assertEquals(69.0, summary.totalInventoryValue, 0.001)
        assertEquals(2, summary.categoryCounts[InventoryCategory.FOOD])
        assertEquals(1, summary.categoryCounts[InventoryCategory.EQUIPMENT])
        assertEquals(1, summary.categoryCounts[InventoryCategory.CONSUMABLES])
    }

    @Test
    fun testStockInOperation() {
        val item = InventoryItemEntity(
            id = "item-seed",
            sku = "SKU-SEED",
            name = "Canary Seed",
            category = InventoryCategory.FOOD,
            currentStock = 5.0,
            costPerUnit = 3.0
        )

        val (updatedItem, transaction) = InventoryAnalyticsHelper.buildStockIn(
            item = item,
            quantity = 15.0,
            unitPrice = 3.5,
            notes = "Supplier delivery #104"
        )

        assertEquals(20.0, updatedItem.currentStock, 0.001)
        assertEquals(3.5, updatedItem.costPerUnit, 0.001)

        assertEquals("item-seed", transaction.itemId)
        assertEquals(StockTransactionType.STOCK_IN, transaction.transactionType)
        assertEquals(15.0, transaction.quantity, 0.001)
        assertEquals(5.0, transaction.previousStock, 0.001)
        assertEquals(20.0, transaction.newStock, 0.001)
        assertEquals(52.5, transaction.totalCost, 0.001) // 15.0 * 3.5
        assertEquals("Supplier delivery #104", transaction.notes)
    }

    @Test
    fun testStockOutOperation() {
        val item = InventoryItemEntity(
            id = "item-med",
            sku = "SKU-MED",
            name = "Vitamins",
            category = InventoryCategory.SUPPLEMENTS,
            currentStock = 8.0,
            costPerUnit = 10.0
        )

        val (updatedItem, transaction) = InventoryAnalyticsHelper.buildStockOut(
            item = item,
            quantity = 3.0,
            reason = "Expired bottle",
            notes = "Disposed per protocol"
        )

        assertEquals(5.0, updatedItem.currentStock, 0.001)
        assertEquals(StockTransactionType.STOCK_OUT, transaction.transactionType)
        assertEquals(3.0, transaction.quantity, 0.001)
        assertEquals(8.0, transaction.previousStock, 0.001)
        assertEquals(5.0, transaction.newStock, 0.001)
        assertTrue(transaction.notes?.contains("Expired bottle") == true)
    }

    @Test
    fun testStockOutCannotDropBelowZero() {
        val item = InventoryItemEntity(
            id = "item-pack",
            sku = "SKU-PACK",
            name = "Nest Liners",
            currentStock = 2.0,
            costPerUnit = 1.0
        )

        val (updatedItem, transaction) = InventoryAnalyticsHelper.buildStockOut(
            item = item,
            quantity = 10.0,
            reason = "Damaged during flood"
        )

        assertEquals(0.0, updatedItem.currentStock, 0.001)
        assertEquals(0.0, transaction.newStock, 0.001)
    }

    @Test
    fun testConsumptionLoggingWithInventoryReduction() {
        val item = InventoryItemEntity(
            id = "item-eggfood",
            sku = "SKU-EGGF",
            name = "Egg Food Blend",
            category = InventoryCategory.FOOD,
            currentStock = 12.0,
            costPerUnit = 4.0
        )

        val (updatedItem, transaction) = InventoryAnalyticsHelper.buildConsumption(
            item = item,
            quantity = 2.5,
            referenceType = "NUTRITION",
            referenceId = "CAGE-A1",
            notes = "Breeding preparation diet"
        )

        assertEquals(9.5, updatedItem.currentStock, 0.001)
        assertEquals(StockTransactionType.CONSUMPTION, transaction.transactionType)
        assertEquals(2.5, transaction.quantity, 0.001)
        assertEquals(12.0, transaction.previousStock, 0.001)
        assertEquals(9.5, transaction.newStock, 0.001)
        assertEquals(10.0, transaction.totalCost, 0.001) // 2.5 * 4.0
        assertEquals("NUTRITION", transaction.referenceType)
        assertEquals("CAGE-A1", transaction.referenceId)
    }

    // =========================================================================
    // FINANCE MODULE TESTS
    // =========================================================================

    @Test
    fun testProfitAndLossCalculation() {
        val expenses = listOf(
            ExpenseEntity(title = "Seed Bag", category = ExpenseCategory.FOOD, amount = 120.0),
            ExpenseEntity(title = "Supplements", category = ExpenseCategory.SUPPLEMENTS, amount = 45.0),
            ExpenseEntity(title = "Mite Treatment", category = ExpenseCategory.MEDICINE, amount = 35.0),
            ExpenseEntity(title = "Show Cages", category = ExpenseCategory.EQUIPMENT, amount = 150.0),
            ExpenseEntity(title = "New English Breeder Cock", category = ExpenseCategory.BIRD_PURCHASE, amount = 200.0),
            ExpenseEntity(title = "Electricity share", category = ExpenseCategory.OTHER, amount = 50.0),
            ExpenseEntity(title = "Old voided bill", category = ExpenseCategory.OTHER, amount = 100.0, isDeleted = true)
        )

        val incomes = listOf(
            IncomeEntity(title = "Sold Adult Pair", category = IncomeCategory.BIRD_SALE, amount = 300.0),
            IncomeEntity(title = "Sold Baby Budgie 1", category = IncomeCategory.CHICK_SALE, amount = 80.0),
            IncomeEntity(title = "Sold Baby Budgie 2", category = IncomeCategory.CHICK_SALE, amount = 85.0),
            IncomeEntity(title = "Club Prize Award", category = IncomeCategory.OTHER, amount = 50.0),
            IncomeEntity(title = "Voided sale", category = IncomeCategory.OTHER, amount = 500.0, isDeleted = true)
        )

        val pnl = FinanceAnalyticsHelper.computeProfitAndLoss(
            expenses = expenses,
            incomes = incomes,
            totalPairsCount = 10,
            totalChicksCount = 20
        )

        // Total Expenses: 120 + 45 + 35 + 150 + 200 + 50 = 600.0 (excludes deleted 100.0)
        assertEquals(600.0, pnl.totalExpenses, 0.001)

        // Total Income: 300 + 80 + 85 + 50 = 515.0 (excludes deleted 500.0)
        assertEquals(515.0, pnl.totalIncome, 0.001)

        // Net Profit = 515 - 600 = -85.0 (Operating loss)
        assertEquals(-85.0, pnl.netProfit, 0.001)
        assertEquals(ProfitStatus.LOSS, pnl.status)

        // Category breakdown verification
        assertEquals(120.0, pnl.expenseByCategory[ExpenseCategory.FOOD] ?: 0.0, 0.001)
        assertEquals(45.0, pnl.expenseByCategory[ExpenseCategory.SUPPLEMENTS] ?: 0.0, 0.001)
        assertEquals(35.0, pnl.expenseByCategory[ExpenseCategory.MEDICINE] ?: 0.0, 0.001)
        assertEquals(150.0, pnl.expenseByCategory[ExpenseCategory.EQUIPMENT] ?: 0.0, 0.001)
        assertEquals(200.0, pnl.expenseByCategory[ExpenseCategory.BIRD_PURCHASE] ?: 0.0, 0.001)
        assertEquals(50.0, pnl.expenseByCategory[ExpenseCategory.OTHER] ?: 0.0, 0.001)

        assertEquals(300.0, pnl.incomeByCategory[IncomeCategory.BIRD_SALE] ?: 0.0, 0.001)
        assertEquals(165.0, pnl.incomeByCategory[IncomeCategory.CHICK_SALE] ?: 0.0, 0.001) // 80 + 85
        assertEquals(50.0, pnl.incomeByCategory[IncomeCategory.OTHER] ?: 0.0, 0.001)

        // Breeding expenses: Food (120) + Supplements (45) + Medicine (35) = 200.0
        // Cost per pair = 200 / 10 pairs = 20.0
        assertNotNull(pnl.costPerPair)
        assertEquals(20.0, pnl.costPerPair ?: 0.0, 0.001)

        // Cost per chick = 200 / 20 chicks = 10.0
        assertNotNull(pnl.costPerChick)
        assertEquals(10.0, pnl.costPerChick ?: 0.0, 0.001)

        // Revenue per chick sold = 165 / 2 chicks sold = 82.5
        assertNotNull(pnl.revenuePerChickSold)
        assertEquals(82.5, pnl.revenuePerChickSold ?: 0.0, 0.001)
    }

    @Test
    fun testMonthlyAndYearlyFinanceSummaries() {
        val cal = java.util.Calendar.getInstance()

        // 2026-08 records
        cal.set(2026, java.util.Calendar.AUGUST, 15, 12, 0)
        val augDate = cal.timeInMillis
        val augExp = ExpenseEntity(title = "Aug Feed", category = ExpenseCategory.FOOD, amount = 100.0, date = augDate)
        val augInc = IncomeEntity(title = "Aug Sale", category = IncomeCategory.BIRD_SALE, amount = 250.0, date = augDate)

        // 2026-09 records
        cal.set(2026, java.util.Calendar.SEPTEMBER, 10, 12, 0)
        val sepDate = cal.timeInMillis
        val sepExp = ExpenseEntity(title = "Sep Feed", category = ExpenseCategory.FOOD, amount = 150.0, date = sepDate)
        val sepInc = IncomeEntity(title = "Sep Chick Sale", category = IncomeCategory.CHICK_SALE, amount = 200.0, date = sepDate)

        val expenses = listOf(augExp, sepExp)
        val incomes = listOf(augInc, sepInc)

        val monthlySummaries = FinanceAnalyticsHelper.computeMonthlySummaries(expenses, incomes)

        assertEquals(2, monthlySummaries.size)

        // Sorted descending: first should be 2026-09, then 2026-08
        val sepSummary = monthlySummaries[0]
        assertEquals("2026-09", sepSummary.yearMonth)
        assertEquals(200.0, sepSummary.totalIncome, 0.001)
        assertEquals(150.0, sepSummary.totalExpenses, 0.001)
        assertEquals(50.0, sepSummary.netProfit, 0.001)

        val augSummary = monthlySummaries[1]
        assertEquals("2026-08", augSummary.yearMonth)
        assertEquals(250.0, augSummary.totalIncome, 0.001)
        assertEquals(100.0, augSummary.totalExpenses, 0.001)
        assertEquals(150.0, augSummary.netProfit, 0.001)

        // Yearly summaries
        val yearlySummaries = FinanceAnalyticsHelper.computeYearlySummaries(monthlySummaries)
        assertEquals(1, yearlySummaries.size)
        val y2026 = yearlySummaries[0]
        assertEquals(2026, y2026.year)
        assertEquals(450.0, y2026.totalIncome, 0.001) // 250 + 200
        assertEquals(250.0, y2026.totalExpenses, 0.001) // 100 + 150
        assertEquals(200.0, y2026.netProfit, 0.001) // 450 - 250
        assertEquals(2, y2026.monthlySummaries.size)
    }
}
