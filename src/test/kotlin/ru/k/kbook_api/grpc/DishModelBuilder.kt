package ru.k.kbook_api.grpc

import ru.k.kbook_api.grpc.dish.CreateDishRequest
import ru.k.kbook_api.grpc.dish.DeleteDishRequest
import ru.k.kbook_api.grpc.dish.DishCategory
import ru.k.kbook_api.grpc.dish.DishFlag
import ru.k.kbook_api.grpc.dish.DishImage
import ru.k.kbook_api.grpc.dish.DishProduct
import ru.k.kbook_api.grpc.dish.GetDishRequest
import ru.k.kbook_api.grpc.dish.ListDishesRequest
import ru.k.kbook_api.grpc.dish.UpdateDishRequest
import ru.k.kbook_api.grpc.dish.ValidateDishResponse

object DishModelBuilder {

    fun createDishRequest(name: String = "Test Dish"): CreateDishRequest = CreateDishRequest.newBuilder()
        .setName(name)
        .setPortionSize(100.0)
        .setCategory(DishCategory.SECOND)
        .addAllComposition(emptyList())
        .build()

    fun getDishRequest(id: Long): GetDishRequest = GetDishRequest.newBuilder()
        .setId(id)
        .build()

    fun updateDishRequest(id: Long, name: String? = null): UpdateDishRequest = UpdateDishRequest.newBuilder()
        .setId(id)
        .also { if (name != null) it.setName(name) }
        .build()

    fun deleteDishRequest(id: Long): DeleteDishRequest = DeleteDishRequest.newBuilder()
        .setId(id)
        .build()

    fun listDishesRequest(
        searchQuery: String? = null,
        categories: List<DishCategory> = emptyList(),
        flags: List<DishFlag> = emptyList(),
        limit: Int? = null,
        offset: Int? = null
    ): ListDishesRequest = ListDishesRequest.newBuilder()
        .also { request ->
            searchQuery?.let { request.setSearchQuery(it) }
            request.addAllCategories(categories)
            request.addAllFlags(flags)
        }
        .build()

    fun dishImage(url: String = "http://example.com/image.jpg"): DishImage = DishImage.newBuilder()
        .setUrl(url)
        .setContentType("url")
        .build()

    fun dishProduct(productId: Long, quantity: Double = 100.0, productName: String = "Test Product"): DishProduct =
        DishProduct.newBuilder()
            .setProductId(productId)
            .setQuantity(quantity)
            .setProductName(productName)
            .build()

    fun validateDishResponse(
        valid: Boolean = true,
        errors: List<String> = emptyList(),
        calculatedCaloricity: Double = 0.0,
        calculatedProtein: Double = 0.0,
        calculatedFat: Double = 0.0,
        calculatedCarb: Double = 0.0,
        availableFlags: List<DishFlag> = emptyList()
    ): ValidateDishResponse = ValidateDishResponse.newBuilder()
        .setValid(valid)
        .addAllErrors(errors)
        .setCalculatedCaloricity(calculatedCaloricity)
        .setCalculatedProtein(calculatedProtein)
        .setCalculatedFat(calculatedFat)
        .setCalculatedCarb(calculatedCarb)
        .addAllAvailableFlags(availableFlags)
        .build()

    fun createDishRequestWithComposition(
        name: String = "Test Dish",
        portionSize: Double = 100.0,
        category: DishCategory = DishCategory.SECOND,
        composition: List<DishProduct> = listOf(dishProduct(1))
    ): CreateDishRequest = CreateDishRequest.newBuilder()
        .setName(name)
        .setPortionSize(portionSize)
        .setCategory(category)
        .addAllComposition(composition)
        .build()

}
