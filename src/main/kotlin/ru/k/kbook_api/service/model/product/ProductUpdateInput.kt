package ru.k.kbook_api.service.model.product

data class ProductUpdateInput(
    val id: Long,
    val name: String? = null,
    val images: List<ImageInput>? = null,
    val caloricity: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null,
    val carb: Double? = null,
    val description: String? = null,
    val category: ProductCategory? = null,
    val cookingRequired: CookingRequired? = null,
    val flags: Set<ProductFlag>? = null
) {
    init {
        require(id > 0) { "ID продукта должен быть положительным числом" }
        name?.let { require(it.length >= 2) { "Название должно быть минимум 2 символа" } }
        caloricity?.let { require(it >= 0) { "Калорийность не может быть отрицательной" } }
        protein?.let { require(it in 0.0..100.0) { "Белки должны быть от 0 до 100 г" } }
        fat?.let { require(it in 0.0..100.0) { "Жиры должны быть от 0 до 100 г" } }
        carb?.let { require(it in 0.0..100.0) { "Углеводы должны быть от 0 до 100 г" } }
        images?.let { require(it.size <= 5) { "Максимальное количество изображений: 5" } }
    }
}