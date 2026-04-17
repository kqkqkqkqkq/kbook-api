package ru.k.kbook_api.mapper

import kotlinx.datetime.toKotlinLocalDateTime
import ru.k.kbook_api.repository.entity.product.ContentTypeDbo
import ru.k.kbook_api.repository.entity.product.CookingRequiredDbo
import ru.k.kbook_api.repository.entity.product.ProductCategoryDbo
import ru.k.kbook_api.repository.entity.product.ProductDbo
import ru.k.kbook_api.repository.entity.product.ProductFlagDbo
import ru.k.kbook_api.repository.entity.product.ProductImageDbo
import ru.k.kbook_api.service.model.product.ContentType
import ru.k.kbook_api.service.model.product.CookingRequired
import ru.k.kbook_api.service.model.product.Product
import ru.k.kbook_api.service.model.product.ProductCategory
import ru.k.kbook_api.service.model.product.ProductFlag
import ru.k.kbook_api.service.model.product.ProductImage

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

fun Product.toProductDbo(): ProductDbo {
    val productDbo = ProductDbo(
        id = id,
        name = name,
        images = images.map { it.toProductImageDbo() }.toSet().toMutableList(),
        caloricity = caloricity,
        protein = protein,
        fat = fat,
        carb = carb,
        description = description,
        category = category.toProductCategoryDbo(),
        cookingRequired = cookingRequired.toCookingRequiredDbo(),
        flags = flags.map { it.toProductFlagDbo() }.toMutableSet()
    )
    productDbo.images.forEach { it.product = productDbo }
    return productDbo
}

fun ProductImageDbo.toProductImage() = ProductImage(
    id = id,
    productId = product?.id,
    url = url,
    image = image,
    contentType = contentType.toContentType()
)

fun ProductImage.toProductImageDbo() = ProductImageDbo(
    id = id,
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
