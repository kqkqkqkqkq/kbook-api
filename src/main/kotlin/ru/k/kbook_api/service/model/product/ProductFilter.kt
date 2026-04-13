package ru.k.kbook_api.service.model.product

data class ProductFilter(
    val searchQuery: String? = null,
    val categories: Set<ProductCategory> = emptySet(),
    val cookingRequired: Set<CookingRequired> = emptySet(),
    val flags: Set<ProductFlag> = emptySet(),
    val sortBy: SortField = SortField.NAME,
    val sortDirection: SortDirection = SortDirection.ASC,
    val limit: Int = 20,
    val offset: Int = 0
) {
    init {
        require(limit in 1..100) { "Лимит должен быть от 1 до 100" }
        require(offset >= 0) { "Смещение не может быть отрицательным" }
    }
}

enum class SortField {
    NAME,
    CALORICITY,
    PROTEIN,
    FAT,
    CARB
}

enum class SortDirection {
    ASC,
    DESC
}