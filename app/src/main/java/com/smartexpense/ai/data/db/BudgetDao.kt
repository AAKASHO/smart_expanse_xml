package com.smartexpense.ai.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year LIMIT 1")
    fun getBudget(month: Int, year: Int): Flow<Budget?>

    @Query("SELECT * FROM budgets ORDER BY year DESC, month DESC LIMIT 1")
    fun getLatestBudget(): Flow<Budget?>
}
