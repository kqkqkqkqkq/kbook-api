package ru.k.kbook_api.grpc

import ru.k.kbook_api.grpc.product.ContentTypeDto
import ru.k.kbook_api.grpc.product.CookingRequiredDto
import ru.k.kbook_api.grpc.product.CreateProductRequest
import ru.k.kbook_api.grpc.product.DeleteProductRequest
import ru.k.kbook_api.grpc.product.GetProductRequest
import ru.k.kbook_api.grpc.product.GetProductsForDishRequest
import ru.k.kbook_api.grpc.product.ImageInput
import ru.k.kbook_api.grpc.product.ListProductsRequest
import ru.k.kbook_api.grpc.product.ProductCategoryDto
import ru.k.kbook_api.grpc.product.ProductFlagDto
import ru.k.kbook_api.grpc.product.SortDirectionDto
import ru.k.kbook_api.grpc.product.SortFieldDto
import ru.k.kbook_api.grpc.product.UpdateProductRequest

object ProductModelBuilder {
    fun getProductRequest(id: Long): GetProductRequest = GetProductRequest
        .newBuilder()
        .setId(id)
        .build()

    fun createProductRequest(name: String): CreateProductRequest = CreateProductRequest
        .newBuilder()
        .setName(name)
        .setCaloricity(0.0)
        .setProtein(0.0)
        .setFat(0.0)
        .setCarb(0.0)
        .addAllImages(emptyList())
        .setDescription("Description")
        .setCategory(ProductCategoryDto.VEGETABLES)
        .setCookingRequired(CookingRequiredDto.REQUIRES_COOKING)
        .addAllFlags(emptyList())
        .build()

    fun listProductsRequest(): ListProductsRequest = ListProductsRequest
        .newBuilder()
        .build()

    fun updateProductRequest(id: Long, name: String? = null): UpdateProductRequest = UpdateProductRequest.newBuilder()
        .setId(id)
        .also { if (name != null) it.setName(name) }
        .build()

    fun deleteProductRequest(id: Long): DeleteProductRequest = DeleteProductRequest.newBuilder()
        .setId(id)
        .build()

    fun listProductsRequest(
        searchQuery: String? = null,
        categories: List<ProductCategoryDto> = emptyList(),
        cookingRequired: List<CookingRequiredDto> = emptyList(),
        flags: List<ProductFlagDto> = emptyList(),
        sortBy: SortFieldDto? = null,
        sortDirection: SortDirectionDto? = null,
        limit: Int? = null,
        offset: Int? = null
    ):ListProductsRequest  = ListProductsRequest.newBuilder()
        .also { request ->
            searchQuery?.let { request.setSearchQuery(it) }
            request.addAllCategories(categories)
            request.addAllCookingRequired(cookingRequired)
            request.addAllFlags(flags)
            sortBy?.let { request.setSortBy(it) }
            sortDirection?.let { request.setSortDirection(it) }
            limit?.let { request.setLimit(it) }
            offset?.let { request.setOffset(it) }
        }
        .build()

    fun getProductsForDishRequest(ids: List<Long>): GetProductsForDishRequest = GetProductsForDishRequest.newBuilder()
        .addAllProductIds(ids)
        .build()

    fun imageInput(url: String, contentType: ContentTypeDto = ContentTypeDto.URL): ImageInput =
        ImageInput.newBuilder()
            .setUrl(url)
            .setContentType(contentType)
            .build()
}
