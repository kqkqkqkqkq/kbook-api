package ru.k.kbook_api.grpc


import org.springframework.stereotype.Service
import ru.k.kbook_api.grpc.product.CreateProductRequest
import ru.k.kbook_api.grpc.product.DeleteProductRequest
import ru.k.kbook_api.grpc.product.DeleteProductResponse
import ru.k.kbook_api.grpc.product.GetProductRequest
import ru.k.kbook_api.grpc.product.GetProductsForDishRequest
import ru.k.kbook_api.grpc.product.ListProductsRequest
import ru.k.kbook_api.grpc.product.ListProductsResponse
import ru.k.kbook_api.grpc.product.ProductDto
import ru.k.kbook_api.grpc.product.ProductResponse
import ru.k.kbook_api.grpc.product.ProductServiceGrpcKt
import ru.k.kbook_api.grpc.product.SortDirectionDto
import ru.k.kbook_api.grpc.product.SortFieldDto
import ru.k.kbook_api.grpc.product.UpdateProductRequest
import ru.k.kbook_api.mapper.mergeWith
import ru.k.kbook_api.mapper.toProduct
import ru.k.kbook_api.mapper.toProductDto
import ru.k.kbook_api.service.ProductInUseException
import ru.k.kbook_api.service.ProductService
import ru.k.kbook_api.service.model.product.CookingRequired
import ru.k.kbook_api.service.model.product.ProductCategory
import ru.k.kbook_api.service.model.product.ProductFlag

@Service
class ProductGrpcService(
    private val productService: ProductService,
) : ProductServiceGrpcKt.ProductServiceCoroutineImplBase() {

    override suspend fun createProduct(request: CreateProductRequest): ProductResponse {
        return try {
            val product = request.toProduct()
            val saved = productService.createProduct(product)
            ProductResponse.newBuilder()
                .setProduct(saved.toProductDto())
                .setSuccess(true)
                .setMessage("Product created successfully")
                .build()
        } catch (e: Exception) {
            ProductResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Failed to create product: ${e.message}")
                .build()
        }
    }

    override suspend fun getProduct(request: GetProductRequest): ProductResponse {
        return try {
            val product = productService.getProductById(request.id)
            ProductResponse.newBuilder()
                .setProduct(product.toProductDto())
                .setSuccess(true)
                .build()
        } catch (e: Exception) {
            ProductResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Product not found: ${e.message}")
                .build()
        }
    }

    override suspend fun updateProduct(request: UpdateProductRequest): ProductResponse {
        return try {
            val existingProduct = productService.getProductById(request.id)
            val updatedProduct = request.mergeWith(existingProduct)
            val saved = productService.updateProduct(request.id, updatedProduct)
            ProductResponse.newBuilder()
                .setProduct(saved.toProductDto())
                .setSuccess(true)
                .setMessage("Product updated successfully")
                .build()
        } catch (e: Exception) {
            ProductResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Failed to update product: ${e.message}")
                .build()
        }
    }

    override suspend fun deleteProduct(request: DeleteProductRequest): DeleteProductResponse {
        return try {
            productService.deleteProduct(request.id)
            DeleteProductResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Product deleted successfully")
                .build()
        } catch (e: ProductInUseException) {
            DeleteProductResponse.newBuilder()
                .setSuccess(false)
                .setMessage(e.message)
                .addAllUsedInDishes(e.dishNames)
                .build()
        } catch (e: Exception) {
            DeleteProductResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Failed to delete product: ${e.message}")
                .build()
        }
    }

    override suspend fun listProducts(request: ListProductsRequest): ListProductsResponse {
        return try {
            val allProducts = productService.getAllProducts().map { it.toProductDto() }
            val filteredProducts = filterAndSortProducts(allProducts, request)
            val total = filteredProducts.size.toLong()
            val offset = if (request.hasOffset()) request.offset.toInt().coerceAtLeast(0) else 0
            val limit = if (request.hasLimit()) request.limit.toInt().coerceAtLeast(0) else filteredProducts.size
            val page = if (limit > 0) filteredProducts.drop(offset).take(limit) else filteredProducts.drop(offset)

            ListProductsResponse.newBuilder()
                .addAllProducts(page)
                .setTotalCount(total)
                .setSuccess(true)
                .build()
        } catch (e: Exception) {
            ListProductsResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Failed to fetch products: ${e.message}")
                .setTotalCount(0)
                .build()
        }
    }

    override suspend fun getProductsForDish(request: GetProductsForDishRequest): ListProductsResponse {
        return try {
            val products = request.productIdsList.map { id ->
                productService.getProductById(id).toProductDto()
            }
            ListProductsResponse.newBuilder()
                .addAllProducts(products)
                .setTotalCount(products.size.toLong())
                .setSuccess(true)
                .build()
        } catch (e: Exception) {
            ListProductsResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Failed to fetch products for dish: ${e.message}")
                .build()
        }
    }

    private fun filterAndSortProducts(
        products: List<ProductDto>,
        request: ListProductsRequest
    ): List<ProductDto> {
        var result = products.asSequence()

        // Поисковый запрос (по названию, частичное совпадение, регистронезависимо)
        if (request.hasSearchQuery()) {
            val query = request.searchQuery.lowercase().trim()
            result = result.filter { it.name.lowercase().contains(query) }
        }

        // Фильтр по категориям
        if (request.categoriesList.isNotEmpty()) {
            val categories = request.categoriesList.mapNotNull { dto ->
                ProductCategory.entries.find { it.name == dto.name }
            }.toSet()
            result = result.filter { it.category.name in categories.map { c -> c.name } }
        }

        // Фильтр по способу приготовления
        if (request.cookingRequiredList.isNotEmpty()) {
            val cookingTypes = request.cookingRequiredList.mapNotNull { dto ->
                CookingRequired.entries.find { it.name == dto.name }
            }.toSet()
            result = result.filter { it.cookingRequired.name in cookingTypes.map { c -> c.name } }
        }

        // Фильтр по флагам (все переданные флаги должны присутствовать)
        if (request.flagsList.isNotEmpty()) {
            val flags = request.flagsList.mapNotNull { dto ->
                ProductFlag.entries.find { it.name == dto.name }
            }.toSet()
            result = result.filter { productDto ->
                flags.all { flag -> productDto.flagsList.any { it.name == flag.name } }
            }
        }

        // Сортировка
        val comparator: Comparator<ProductDto> = when {
            request.hasSortBy() -> when (request.sortBy) {
                SortFieldDto.NAME -> compareBy { it.name }
                SortFieldDto.CALORICITY -> compareBy { it.caloricity }
                SortFieldDto.PROTEIN -> compareBy { it.protein }
                SortFieldDto.FAT -> compareBy { it.fat }
                SortFieldDto.CARB -> compareBy { it.carb }
                else -> compareBy { it.name }
            }
            else -> compareBy { it.name }
        }

        result = when {
            request.hasSortDirection() && request.sortDirection == SortDirectionDto.DESC ->
                result.sortedWith(comparator.reversed())
            else -> result.sortedWith(comparator)
        }

        return result.toList()
    }
}
