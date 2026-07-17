package com.smartexpense.ai.domain.usecase

import com.smartexpense.ai.data.model.CustomCategoryEntity
import com.smartexpense.ai.data.repository.ExpenseRepositoryImpl
import kotlinx.coroutines.flow.Flow

class GetCustomCategoriesUseCase(private val repository: ExpenseRepositoryImpl) {
    operator fun invoke(): Flow<List<CustomCategoryEntity>> = repository.getCustomCategories()
}

class AddCustomCategoryUseCase(private val repository: ExpenseRepositoryImpl) {
    suspend operator fun invoke(entity: CustomCategoryEntity) = repository.addCustomCategory(entity)
}

class DeleteCustomCategoryUseCase(private val repository: ExpenseRepositoryImpl) {
    suspend operator fun invoke(entity: CustomCategoryEntity) = repository.deleteCustomCategory(entity)
}
