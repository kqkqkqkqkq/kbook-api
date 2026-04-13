package ru.k.kbook_api.service.model.dish

import kotlinx.datetime.LocalDateTime
import ru.k.kbook_api.service.model.product.Product

data class Dish(
    val id: Long? = null,

    val name: String,

    val images: List<DishImage> = emptyList(),

    val caloricity: Double,

    val protein: Double,

    val fat: Double,

    val carb: Double,

    val composition: List<DishProduct> = emptyList(),

    val portionSize: Double,

    val category: DishCategory,

    val flags: Set<DishFlag> = emptySet(),

    val createdAt: LocalDateTime? = null,

    val updatedAt: LocalDateTime? = null
)

data class DishImage(
    val id: Long? = null,
    // поля для image: url, contentType и т.д.
    val url: String? = null,
    val image: ByteArray? = null,
    val contentType: ContentType? = null
)

data class DishProduct(
    val productId: Long,
    val product: Product? = null, // опционально для денормализации
    val quantity: Double // г
)

enum class DishCategory {
    DESSERT, FIRST, SECOND, DRINK, SALAD, SOUP, SNACK
}

enum class DishFlag {
    VEGAN, GLUTEN_FREE, SUGAR_FREE
}

enum class ContentType {
    IMAGE, URL
}