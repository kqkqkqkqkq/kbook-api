package ru.k.kbook_api.service.model

data class ProductInput(
    val name: String,
    val images: List<ImageInput> = emptyList(),
    val caloricity: Double,
    val protein: Double,
    val fat: Double,
    val carb: Double,
    val description: String? = null,
    val category: ProductCategory,
    val cookingRequired: CookingRequired,
    val flags: Set<ProductFlag> = emptySet()
) {
    init {
        require(name.length >= 2) { "Название должно быть минимум 2 символа" }
        require(caloricity >= 0) { "Калорийность не может быть отрицательной" }
        require(protein in 0.0..100.0) { "Белки должны быть от 0 до 100 г" }
        require(fat in 0.0..100.0) { "Жиры должны быть от 0 до 100 г" }
        require(carb in 0.0..100.0) { "Углеводы должны быть от 0 до 100 г" }
        require(protein + fat + carb <= 100.0) {
            "Сумма БЖУ не может превышать 100 г на 100 г продукта"
        }
        require(images.size <= 5) { "Максимальное количество изображений: 5" }
    }
}
