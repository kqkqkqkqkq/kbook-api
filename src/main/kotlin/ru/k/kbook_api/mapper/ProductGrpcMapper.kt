package ru.k.kbook_api.mapper

import com.google.protobuf.Timestamp
import com.google.protobuf.kotlin.toByteString
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import ru.k.kbook_api.grpc.product.ContentTypeDto
import ru.k.kbook_api.grpc.product.CookingRequiredDto
import ru.k.kbook_api.grpc.product.CreateProductRequest
import ru.k.kbook_api.grpc.product.ImageInput
import ru.k.kbook_api.grpc.product.ProductCategoryDto
import ru.k.kbook_api.grpc.product.ProductDto
import ru.k.kbook_api.grpc.product.ProductFlagDto
import ru.k.kbook_api.grpc.product.ProductImageDto
import ru.k.kbook_api.grpc.product.UpdateProductRequest
import ru.k.kbook_api.service.model.product.ContentType
import ru.k.kbook_api.service.model.product.CookingRequired
import ru.k.kbook_api.service.model.product.Product
import ru.k.kbook_api.service.model.product.ProductCategory
import ru.k.kbook_api.service.model.product.ProductFlag
import ru.k.kbook_api.service.model.product.ProductImage
import kotlin.time.ExperimentalTime


fun CreateProductRequest.toProduct(): Product {
    return Product(
        id = null,
        name = name,
        images = imagesList.map { it.toProductImage() },
        caloricity = caloricity,
        protein = protein,
        fat = fat,
        carb = carb,
        description = description,
        category = category.toProductCategory(),
        cookingRequired = cookingRequired.toCookingRequired(),
        flags = flagsList.map { it.toProductFlag() }.toSet(),
    )
}

fun UpdateProductRequest.mergeWith(existing: Product): Product {
    return Product(
        id = existing.id,
        name = if (hasName()) name else existing.name,
        images = if (imagesList.isNotEmpty()) imagesList.map { it.toProductImage() } else existing.images,
        caloricity = if (hasCaloricity()) caloricity else existing.caloricity,
        protein = if (hasProtein()) protein else existing.protein,
        fat = if (hasFat()) fat else existing.fat,
        carb = if (hasCarb()) carb else existing.carb,
        description = if (hasDescription()) description else existing.description,
        category = if (hasCategory()) category.toProductCategory() else existing.category,
        cookingRequired = if (hasCookingRequired()) cookingRequired.toCookingRequired() else existing.cookingRequired,
        flags = if (flagsList.isNotEmpty()) flagsList.map { it.toProductFlag() }.toSet() else existing.flags,
        createdAt = existing.createdAt,
    )
}

fun ImageInput.toProductImage(): ProductImage {
    return ProductImage(
        id = null,
        productId = null,
        url = if (hasUrl()) url else "",
        image = if (hasImage()) image.toByteArray() else null,
        contentType = contentType.toContentType()
    )
}

fun ProductCategoryDto.toProductCategory(): ProductCategory = when (this) {
    ProductCategoryDto.FROZEN -> ProductCategory.FROZEN
    ProductCategoryDto.MEAT -> ProductCategory.MEAT
    ProductCategoryDto.VEGETABLES -> ProductCategory.VEGETABLES
    ProductCategoryDto.GREENS -> ProductCategory.GREENS
    ProductCategoryDto.SPICES -> ProductCategory.SPICES
    ProductCategoryDto.CEREALS -> ProductCategory.CEREALS
    ProductCategoryDto.CANNED -> ProductCategory.CANNED
    ProductCategoryDto.LIQUID -> ProductCategory.LIQUID
    ProductCategoryDto.SWEETS -> ProductCategory.SWEETS
    else -> throw IllegalStateException("Unknown product category")
}

fun CookingRequiredDto.toCookingRequired(): CookingRequired = when (this) {
    CookingRequiredDto.READY_TO_EAT -> CookingRequired.READY_TO_EAT
    CookingRequiredDto.SEMI_FINISHED -> CookingRequired.SEMI_FINISHED
    CookingRequiredDto.REQUIRES_COOKING -> CookingRequired.REQUIRES_COOKING
    else -> throw IllegalStateException("Unknown cooking required")
}

fun ProductFlagDto.toProductFlag(): ProductFlag = when (this) {
    ProductFlagDto.VEGAN -> ProductFlag.VEGAN
    ProductFlagDto.GLUTEN_FREE -> ProductFlag.GLUTEN_FREE
    ProductFlagDto.SUGAR_FREE -> ProductFlag.SUGAR_FREE
    else -> throw IllegalStateException("Unknown product flag")
}

fun ContentTypeDto.toContentType(): ContentType = when (this) {
    ContentTypeDto.IMAGE -> ContentType.IMAGE
    ContentTypeDto.URL -> ContentType.URL
    else -> throw IllegalStateException("Unknown content type")
}

fun Product.toProductDto(): ProductDto {
    return ProductDto.newBuilder()
        .setId(id ?: 0)
        .setName(name)
        .addAllImages(images.map { it.toProductImageDto() })
        .setCaloricity(caloricity)
        .setProtein(protein)
        .setFat(fat)
        .setCarb(carb)
        .setDescription(description ?: "")
        .setCategory(category.toProductCategoryDto())
        .setCookingRequired(cookingRequired.toCookingRequiredDto())
        .addAllFlags(flags.map { it.toProductFlagDto() })
        .setCreatedAt(createdAt?.toProtoTimestamp())
        .setUpdatedAt(updatedAt?.toProtoTimestamp())
        .build()
}

fun ProductImage.toProductImageDto(): ProductImageDto {
    return ProductImageDto.newBuilder()
        .setId(id ?: 0)
        .setUrl(url)
        .setImage(image?.toByteString())
        .setContentType(contentType.toContentTypeDto())
        .build()
}

fun ProductCategory.toProductCategoryDto(): ProductCategoryDto = when (this) {
    ProductCategory.FROZEN -> ProductCategoryDto.FROZEN
    ProductCategory.MEAT -> ProductCategoryDto.MEAT
    ProductCategory.VEGETABLES -> ProductCategoryDto.VEGETABLES
    ProductCategory.GREENS -> ProductCategoryDto.GREENS
    ProductCategory.SPICES -> ProductCategoryDto.SPICES
    ProductCategory.CEREALS -> ProductCategoryDto.CEREALS
    ProductCategory.CANNED -> ProductCategoryDto.CANNED
    ProductCategory.LIQUID -> ProductCategoryDto.LIQUID
    ProductCategory.SWEETS -> ProductCategoryDto.SWEETS
}

fun CookingRequired.toCookingRequiredDto(): CookingRequiredDto = when (this) {
    CookingRequired.READY_TO_EAT -> CookingRequiredDto.READY_TO_EAT
    CookingRequired.SEMI_FINISHED -> CookingRequiredDto.SEMI_FINISHED
    CookingRequired.REQUIRES_COOKING -> CookingRequiredDto.REQUIRES_COOKING
}

fun ProductFlag.toProductFlagDto(): ProductFlagDto = when (this) {
    ProductFlag.VEGAN -> ProductFlagDto.VEGAN
    ProductFlag.GLUTEN_FREE -> ProductFlagDto.GLUTEN_FREE
    ProductFlag.SUGAR_FREE -> ProductFlagDto.SUGAR_FREE
}

fun ContentType.toContentTypeDto(): ContentTypeDto = when (this) {
    ContentType.IMAGE -> ContentTypeDto.IMAGE
    ContentType.URL -> ContentTypeDto.URL
}

@OptIn(ExperimentalTime::class)
fun LocalDateTime.toProtoTimestamp(): Timestamp {
    val instant = this.toInstant(TimeZone.UTC)
    return Timestamp.newBuilder()
        .setSeconds(instant.epochSeconds)
        .setNanos(instant.nanosecondsOfSecond)
        .build()
}