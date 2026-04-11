package ru.k.kbook_api.mapper

import kotlinx.datetime.toKotlinLocalDateTime
import ru.k.kbook_api.repository.entity.ContentTypeDbo
import ru.k.kbook_api.repository.entity.CookingRequiredDbo
import ru.k.kbook_api.repository.entity.ProductCategoryDbo
import ru.k.kbook_api.repository.entity.ProductDbo
import ru.k.kbook_api.repository.entity.ProductFlagDbo
import ru.k.kbook_api.repository.entity.ProductImageDbo
import ru.k.kbook_api.service.model.ContentType
import ru.k.kbook_api.service.model.CookingRequired
import ru.k.kbook_api.service.model.Product
import ru.k.kbook_api.service.model.ProductCategory
import ru.k.kbook_api.service.model.ProductFlag
import ru.k.kbook_api.service.model.ProductImage

fun ProductDbo.toProduct() = Product(
    id = id,
    name = name,
    images = images.map { it.toProductImage() }.toList(),
    caloricity = caloricity,
    protein = protein,
    fat = fat,
    carb = carb,
    description = description,
    category = category.toProductCategory(),
    cookingRequired = cookingRequired.toCookingRequired(),
    flags = flags.map { it.toProductFlag() }.toSet(),
    createdAt = createdAt?.toKotlinLocalDateTime(),
    updatedAt = updatedAt?.toKotlinLocalDateTime(),
)

fun ProductImageDbo.toProductImage() = ProductImage(
    id = id,
    productId = productId,
    url = url,
    image = image,
    contentType = contentType.toContentType()
)

fun ContentTypeDbo.toContentType() = when(this) {
    ContentTypeDbo.IMAGE -> ContentType.IMAGE
    ContentTypeDbo.URL -> ContentType.URL
}

fun ProductCategoryDbo.toProductCategory() = when(this) {
    ProductCategoryDbo.FROZEN -> ProductCategory.FROZEN
    ProductCategoryDbo.MEAT -> ProductCategory.MEAT
    ProductCategoryDbo.VEGETABLES -> ProductCategory.VEGETABLES
    ProductCategoryDbo.GREENS -> ProductCategory.GREENS
    ProductCategoryDbo.SPICES -> ProductCategory.SPICES
    ProductCategoryDbo.CEREALS -> ProductCategory.CEREALS
    ProductCategoryDbo.CANNED -> ProductCategory.CANNED
    ProductCategoryDbo.LIQUID -> ProductCategory.LIQUID
    ProductCategoryDbo.SWEETS -> ProductCategory.SWEETS
}

fun CookingRequiredDbo.toCookingRequired() = when(this) {
    CookingRequiredDbo.READY_TO_EAT -> CookingRequired.READY_TO_EAT
    CookingRequiredDbo.SEMI_FINISHED -> CookingRequired.SEMI_FINISHED
    CookingRequiredDbo.REQUIRES_COOKING -> CookingRequired.REQUIRES_COOKING
}

fun ProductFlagDbo.toProductFlag() = when(this) {
    ProductFlagDbo.VEGAN -> ProductFlag.VEGAN
    ProductFlagDbo.GLUTEN_FREE -> ProductFlag.GLUTEN_FREE
    ProductFlagDbo.SUGAR_FREE -> ProductFlag.SUGAR_FREE
}

fun Product.toProductDbo() = ProductDbo(
    id = id,
    name = name,
    caloricity = caloricity,
    protein = protein,
    fat = fat,
    carb = carb,
    description = description,
    category = category.toProductCategoryDbo(),
    cookingRequired = cookingRequired.toCookingRequiredDbo(),
    flags = flags.map { it.toProductFlagDbo() }.toMutableSet()
)

fun ProductImage.toProductImageDbo() = ProductImageDbo(
    id = id,
    productId = productId,
    url = url,
    image = image,
    contentType = contentType.toContentTypeDbo()
)

fun ContentType.toContentTypeDbo() = when(this) {
    ContentType.IMAGE -> ContentTypeDbo.IMAGE
    ContentType.URL -> ContentTypeDbo.URL
}

fun ProductCategory.toProductCategoryDbo() = when(this) {
    ProductCategory.FROZEN -> ProductCategoryDbo.FROZEN
    ProductCategory.MEAT -> ProductCategoryDbo.MEAT
    ProductCategory.VEGETABLES -> ProductCategoryDbo.VEGETABLES
    ProductCategory.GREENS -> ProductCategoryDbo.GREENS
    ProductCategory.SPICES -> ProductCategoryDbo.SPICES
    ProductCategory.CEREALS -> ProductCategoryDbo.CEREALS
    ProductCategory.CANNED -> ProductCategoryDbo.CANNED
    ProductCategory.LIQUID -> ProductCategoryDbo.LIQUID
    ProductCategory.SWEETS -> ProductCategoryDbo.SWEETS
}

fun CookingRequired.toCookingRequiredDbo() = when(this) {
    CookingRequired.READY_TO_EAT -> CookingRequiredDbo.READY_TO_EAT
    CookingRequired.SEMI_FINISHED -> CookingRequiredDbo.SEMI_FINISHED
    CookingRequired.REQUIRES_COOKING -> CookingRequiredDbo.REQUIRES_COOKING
}

fun ProductFlag.toProductFlagDbo() = when(this) {
    ProductFlag.VEGAN -> ProductFlagDbo.VEGAN
    ProductFlag.GLUTEN_FREE -> ProductFlagDbo.GLUTEN_FREE
    ProductFlag.SUGAR_FREE -> ProductFlagDbo.SUGAR_FREE
}
