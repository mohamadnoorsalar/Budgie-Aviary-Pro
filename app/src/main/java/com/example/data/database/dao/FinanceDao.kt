package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.ExpenseEntity
import com.example.data.database.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    // --- Expenses ---
    @Query("SELECT * FROM expenses WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE category = :category AND isDeleted = 0 ORDER BY date DESC")
    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE pairId = :pairId AND isDeleted = 0 ORDER BY date DESC")
    fun getExpensesForPair(pairId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE birdRingNumber = :ringNumber AND isDeleted = 0 ORDER BY date DESC")
    fun getExpensesForBird(ringNumber: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate AND isDeleted = 0 ORDER BY date DESC")
    fun getExpensesInDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE isDeleted = 0")
    fun getTotalExpenses(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE category = :category AND isDeleted = 0")
    fun getTotalExpensesByCategory(category: String): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    // --- Income ---
    @Query("SELECT * FROM income WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllIncome(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM income WHERE category = :category AND isDeleted = 0 ORDER BY date DESC")
    fun getIncomeByCategory(category: String): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM income WHERE soldBirdRingNumber = :ringNumber AND isDeleted = 0 ORDER BY date DESC")
    fun getIncomeForBird(ringNumber: String): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM income WHERE soldChickId = :chickId AND isDeleted = 0 ORDER BY date DESC")
    fun getIncomeForChick(chickId: String): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM income WHERE date BETWEEN :startDate AND :endDate AND isDeleted = 0 ORDER BY date DESC")
    fun getIncomeInDateRange(startDate: Long, endDate: Long): Flow<List<IncomeEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM income WHERE isDeleted = 0")
    fun getTotalIncome(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM income WHERE category = :category AND isDeleted = 0")
    fun getTotalIncomeByCategory(category: String): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity)

    @Update
    suspend fun updateIncome(income: IncomeEntity)

    @Delete
    suspend fun deleteIncome(income: IncomeEntity)
}
