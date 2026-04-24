package ru.k.kbook_api.service

import ru.k.kbook_api.service.model.dish.Dish
import ru.k.kbook_api.service.model.dish.DishCategory
import ru.k.kbook_api.service.model.dish.DishFlag
import ru.k.kbook_api.service.model.dish.DishImage
import ru.k.kbook_api.service.model.dish.DishProduct

interface DishService {
    suspend fun createDish(request: CreateDishRequest): Dish
    suspend fun updateDish(id: Long, request: UpdateDishRequest): Dish
    suspend fun getDish(id: Long): Dish
    suspend fun listDishes(filter: DishFilter): List<Dish>
    suspend fun deleteDish(id: Long)
    suspend fun validateDish(request: CreateDishRequest): ValidateDishResponse
    suspend fun calculateKbju(composition: List<DishProduct>): Kbju
    suspend fun getAvailableFlags(composition: List<DishProduct>): Set<DishFlag>
}

data class CreateDishRequest(
    val name: String,
    val images: List<DishImage> = emptyList(),
    val composition: List<DishProduct>,
    val portionSize: Double,
    val category: DishCategory? = null,
    val flags: Set<DishFlag> = emptySet(),
    val caloricity: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null,
    val carb: Double? = null,
)

data class UpdateDishRequest(
    val name: String? = null,
    val images: List<DishImage>? = null,
    val composition: List<DishProduct>? = null,
    val portionSize: Double? = null,
    val category: DishCategory? = null,
    val flags: Set<DishFlag>? = null,
    val caloricity: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null,
    val carb: Double? = null
)

data class DishFilter(
    val categories: List<DishCategory>? = null,
    val flags: List<DishFlag>? = null,
    val search: String? = null,
    val limit: Int? = null,
    val offset: Int? = null,
)

data class ValidateDishResponse(
    val valid: Boolean,
    val errors: List<String> = emptyList(),
    val calculatedKbju: Kbju? = null,
    val availableFlags: Set<DishFlag> = emptySet()
)

data class Kbju(
    val caloricity: Double,
    val protein: Double,
    val fat: Double,
    val carb: Double
)