package ru.k.kbook_api.mapper

import kotlinx.datetime.toKotlinLocalDateTime
import ru.k.kbook_api.repository.entity.dish.DishCategoryDbo
import ru.k.kbook_api.repository.entity.dish.DishDbo
import ru.k.kbook_api.repository.entity.dish.DishFlagDbo
import ru.k.kbook_api.repository.entity.dish.DishImageDbo
import ru.k.kbook_api.repository.entity.product.ContentTypeDbo
import ru.k.kbook_api.service.model.dish.ContentType
import ru.k.kbook_api.service.model.dish.Dish
import ru.k.kbook_api.service.model.dish.DishCategory
import ru.k.kbook_api.service.model.dish.DishFlag
import ru.k.kbook_api.service.model.dish.DishImage
import ru.k.kbook_api.service.model.dish.DishProduct

fun DishDbo.toDish(): Dish = Dish(
    id = id,
    name = name,
    images = images.map { it.toDishImage() },
    caloricity = caloricity,
    protein = protein,
    fat = fat,
    carb = carb,
    composition = products.map { row ->
        DishProduct(
            productId = row.product.id!!,
            product = row.product.toProduct(),
            quantity = row.quantity,
        )
    },
    portionSize = portionSize,
    category = category.toDishCategory(),
    flags = flags.map { it.toDishFlag() }.toSet(),
    createdAt = createdAt?.toKotlinLocalDateTime(),
    updatedAt = updatedAt?.toKotlinLocalDateTime(),
)

fun DishImageDbo.toDishImage(): DishImage = DishImage(
    id = id,
    url = url,
    image = image,
    contentType = contentType.toDishContentType(),
)

fun ContentTypeDbo.toDishContentType(): ContentType = when (this) {
    ContentTypeDbo.IMAGE -> ContentType.IMAGE
    ContentTypeDbo.URL -> ContentType.URL
}

fun DishCategoryDbo.toDishCategory(): DishCategory = when (this) {
    DishCategoryDbo.DESSERT -> DishCategory.DESSERT
    DishCategoryDbo.FIRST -> DishCategory.FIRST
    DishCategoryDbo.SECOND -> DishCategory.SECOND
    DishCategoryDbo.DRINK -> DishCategory.DRINK
    DishCategoryDbo.SALAD -> DishCategory.SALAD
    DishCategoryDbo.SOUP -> DishCategory.SOUP
    DishCategoryDbo.SNACK -> DishCategory.SNACK
}

fun DishCategory.toDishCategoryDbo(): DishCategoryDbo = when (this) {
    DishCategory.DESSERT -> DishCategoryDbo.DESSERT
    DishCategory.FIRST -> DishCategoryDbo.FIRST
    DishCategory.SECOND -> DishCategoryDbo.SECOND
    DishCategory.DRINK -> DishCategoryDbo.DRINK
    DishCategory.SALAD -> DishCategoryDbo.SALAD
    DishCategory.SOUP -> DishCategoryDbo.SOUP
    DishCategory.SNACK -> DishCategoryDbo.SNACK
}

fun DishFlagDbo.toDishFlag(): DishFlag = when (this) {
    DishFlagDbo.VEGAN -> DishFlag.VEGAN
    DishFlagDbo.GLUTEN_FREE -> DishFlag.GLUTEN_FREE
    DishFlagDbo.SUGAR_FREE -> DishFlag.SUGAR_FREE
}

fun DishFlag.toDishFlagDbo(): DishFlagDbo = when (this) {
    DishFlag.VEGAN -> DishFlagDbo.VEGAN
    DishFlag.GLUTEN_FREE -> DishFlagDbo.GLUTEN_FREE
    DishFlag.SUGAR_FREE -> DishFlagDbo.SUGAR_FREE
}

fun DishImage.toDishImageDbo(dish: DishDbo): DishImageDbo = DishImageDbo(
    id = id,
    dish = dish,
    url = url,
    image = image,
    contentType = (contentType ?: ContentType.URL).toDishContentTypeDbo(),
)

fun ContentType.toDishContentTypeDbo(): ContentTypeDbo = when (this) {
    ContentType.IMAGE -> ContentTypeDbo.IMAGE
    ContentType.URL -> ContentTypeDbo.URL
}
