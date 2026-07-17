package com.smartexpense.ai.data.db

import androidx.room.*
import com.smartexpense.ai.data.model.CustomCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomCategoryDao {

    @Query("SELECT * FROM custom_categories ORDER BY label ASC")
    fun getAllCategories(): Flow<List<CustomCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CustomCategoryEntity)

    @Delete
    suspend fun delete(category: CustomCategoryEntity)

    @Query("DELETE FROM custom_categories WHERE `key` = :key")
    suspend fun deleteByKey(key: String)
}
