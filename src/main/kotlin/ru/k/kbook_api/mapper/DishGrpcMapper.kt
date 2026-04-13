package ru.k.kbook_api.mapper

import com.google.protobuf.ByteString
import kotlin.time.ExperimentalTime
import ru.k.kbook_api.grpc.dish.CreateDishRequest as GrpcCreateDishRequest
import ru.k.kbook_api.grpc.dish.Dish as GrpcDish
import ru.k.kbook_api.grpc.dish.DishCategory as GrpcDishCategory
import ru.k.kbook_api.grpc.dish.DishFilter as GrpcDishFilter
import ru.k.kbook_api.grpc.dish.DishFlag as GrpcDishFlag
import ru.k.kbook_api.grpc.dish.DishImage as GrpcDishImage
import ru.k.kbook_api.grpc.dish.DishListResponse
import ru.k.kbook_api.grpc.dish.DishProduct as GrpcDishProduct
import ru.k.kbook_api.grpc.dish.UpdateDishRequest as GrpcUpdateDishRequest
import ru.k.kbook_api.grpc.dish.ValidateDishResponse as GrpcValidateDishResponse
import ru.k.kbook_api.service.CreateDishRequest
import ru.k.kbook_api.service.UpdateDishRequest
import ru.k.kbook_api.service.ValidateDishResponse
import ru.k.kbook_api.service.DishFilter
import ru.k.kbook_api.service.model.dish.ContentType
import ru.k.kbook_api.service.model.dish.Dish
import ru.k.kbook_api.service.model.dish.DishCategory
import ru.k.kbook_api.service.model.dish.DishFlag
import ru.k.kbook_api.service.model.dish.DishImage
import ru.k.kbook_api.service.model.dish.DishProduct

fun GrpcCreateDishRequest.toServiceCreateDishRequest(): CreateDishRequest = CreateDishRequest(
    name = name,
    images = imagesList.map { it.toServiceDishImage() },
    composition = compositionList.map { it.toServiceDishProduct() },
    portionSize = portionSize,
    category = if (hasCategory()) category.toServiceDishCategory() else null,
    flags = flagsList.map { it.toServiceDishFlag() }.toSet(),
)

fun GrpcUpdateDishRequest.toServiceUpdateDishRequest(): UpdateDishRequest = UpdateDishRequest(
    name = if (hasName()) name else null,
    images = if (imagesCount > 0) imagesList.map { it.toServiceDishImage() } else null,
    composition = if (compositionCount > 0) compositionList.map { it.toServiceDishProduct() } else null,
    portionSize = if (hasPortionSize()) portionSize else null,
    category = if (hasCategory()) category.toServiceDishCategory() else null,
    flags = if (flagsCount > 0) flagsList.map { it.toServiceDishFlag() }.toSet() else null,
    caloricity = if (hasCaloricity()) caloricity else null,
    protein = if (hasProtein()) protein else null,
    fat = if (hasFat()) fat else null,
    carb = if (hasCarb()) carb else null,
)

fun GrpcDishFilter.toServiceDishFilter(): DishFilter = DishFilter(
    categories = if (categoriesCount > 0) categoriesList.map { it.toServiceDishCategory() } else null,
    flags = if (flagsCount > 0) flagsList.map { it.toServiceDishFlag() } else null,
    search = search.takeIf { it.isNotBlank() },
)

fun GrpcDishImage.toServiceDishImage(): DishImage = DishImage(
    id = if (id != 0L) id else null,
    url = url.takeIf { it.isNotBlank() },
    image = if (!image.isEmpty) image.toByteArray() else null,
    contentType = when {
        url.isNotBlank() -> ContentType.URL
        !image.isEmpty -> ContentType.IMAGE
        else -> ContentType.URL
    },
)

fun GrpcDishProduct.toServiceDishProduct(): DishProduct = DishProduct(
    productId = productId,
    product = null,
    quantity = quantity,
)

fun GrpcDishCategory.toServiceDishCategory(): DishCategory = when (this) {
    GrpcDishCategory.DESSERT -> DishCategory.DESSERT
    GrpcDishCategory.FIRST -> DishCategory.FIRST
    GrpcDishCategory.SECOND -> DishCategory.SECOND
    GrpcDishCategory.DRINK -> DishCategory.DRINK
    GrpcDishCategory.SALAD -> DishCategory.SALAD
    GrpcDishCategory.SOUP -> DishCategory.SOUP
    GrpcDishCategory.SNACK -> DishCategory.SNACK
    GrpcDishCategory.UNRECOGNIZED -> throw IllegalArgumentException("Неизвестная категория блюда")
}

fun GrpcDishFlag.toServiceDishFlag(): DishFlag = when (this) {
    GrpcDishFlag.VEGAN -> DishFlag.VEGAN
    GrpcDishFlag.GLUTEN_FREE -> DishFlag.GLUTEN_FREE
    GrpcDishFlag.SUGAR_FREE -> DishFlag.SUGAR_FREE
    GrpcDishFlag.UNRECOGNIZED -> throw IllegalArgumentException("Неизвестный флаг блюда")
}

fun DishCategory.toGrpcDishCategory(): GrpcDishCategory = when (this) {
    DishCategory.DESSERT -> GrpcDishCategory.DESSERT
    DishCategory.FIRST -> GrpcDishCategory.FIRST
    DishCategory.SECOND -> GrpcDishCategory.SECOND
    DishCategory.DRINK -> GrpcDishCategory.DRINK
    DishCategory.SALAD -> GrpcDishCategory.SALAD
    DishCategory.SOUP -> GrpcDishCategory.SOUP
    DishCategory.SNACK -> GrpcDishCategory.SNACK
}

fun DishFlag.toGrpcDishFlag(): GrpcDishFlag = when (this) {
    DishFlag.VEGAN -> GrpcDishFlag.VEGAN
    DishFlag.GLUTEN_FREE -> GrpcDishFlag.GLUTEN_FREE
    DishFlag.SUGAR_FREE -> GrpcDishFlag.SUGAR_FREE
}

@OptIn(ExperimentalTime::class)
fun Dish.toGrpcDish(): GrpcDish {
    val b = GrpcDish.newBuilder()
        .setId(id ?: 0L)
        .setName(name)
        .setCaloricity(caloricity)
        .setProtein(protein)
        .setFat(fat)
        .setCarb(carb)
        .setPortionSize(portionSize)
        .setCategory(category.toGrpcDishCategory())
    images.forEach { b.addImages(it.toGrpcDishImage()) }
    composition.forEach { b.addComposition(it.toGrpcDishProduct()) }
    flags.forEach { b.addFlags(it.toGrpcDishFlag()) }
    createdAt?.let { b.setCreatedAt(it.toProtoTimestamp()) }
    updatedAt?.let { b.setUpdatedAt(it.toProtoTimestamp()) }
    return b.build()
}

fun DishImage.toGrpcDishImage(): GrpcDishImage {
    val b = GrpcDishImage.newBuilder().setId(id ?: 0L)
    url?.let { b.setUrl(it) }
    image?.let { b.setImage(ByteString.copyFrom(it)) }
    b.setContentType(
        when (contentType) {
            ContentType.IMAGE -> "image/binary"
            ContentType.URL -> "url"
            null -> "url"
        },
    )
    return b.build()
}

fun DishProduct.toGrpcDishProduct(): GrpcDishProduct = GrpcDishProduct.newBuilder()
    .setProductId(productId)
    .setProductName(product?.name.orEmpty())
    .setQuantity(quantity)
    .build()

fun ValidateDishResponse.toGrpcValidateDishResponse(): GrpcValidateDishResponse {
    val b = GrpcValidateDishResponse.newBuilder()
        .setValid(valid)
        .addAllErrors(errors)
    calculatedKbju?.let {
        b.setCalculatedCaloricity(it.caloricity)
        b.setCalculatedProtein(it.protein)
        b.setCalculatedFat(it.fat)
        b.setCalculatedCarb(it.carb)
    }
    availableFlags.forEach { b.addAvailableFlags(it.toGrpcDishFlag()) }
    return b.build()
}

fun List<Dish>.toGrpcDishListResponse(): DishListResponse {
    val b = DishListResponse.newBuilder().setTotal(size)
    forEach { b.addDishes(it.toGrpcDish()) }
    return b.build()
}
