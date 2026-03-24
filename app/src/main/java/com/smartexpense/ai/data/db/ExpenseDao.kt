package com.smartexpense.ai.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT :limit")
    fun getRecentExpenses(limit: Int = 5): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getByCategory(category: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalSpending(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE date BETWEEN :startDate AND :endDate GROUP BY category ORDER BY total DESC")
    fun getCategoryTotals(startDate: Long, endDate: Long): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM expenses WHERE merchant LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY date DESC")
    fun search(query: String): Flow<List<Expense>>

    @Query("SELECT date, SUM(amount) as total FROM expenses WHERE date BETWEEN :startDate AND :endDate GROUP BY date / 86400000 ORDER BY date ASC")
    fun getDailyTotals(startDate: Long, endDate: Long): Flow<List<DailyTotal>>

    @Query("SELECT COUNT(*) FROM expenses")
    fun getExpenseCount(): Flow<Int>
}

data class CategoryTotal(
    val category: String,
    val total: Double
)

data class DailyTotal(
    val date: Long,
    val total: Double
)
