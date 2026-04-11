package ru.k.kbook_api.grpc


import org.springframework.stereotype.Service
import ru.k.kbook_api.grpc.product.CreateProductRequest
import ru.k.kbook_api.grpc.product.DeleteProductRequest
import ru.k.kbook_api.grpc.product.DeleteProductResponse
import ru.k.kbook_api.grpc.product.GetProductRequest
import ru.k.kbook_api.grpc.product.GetProductsForDishRequest
import ru.k.kbook_api.grpc.product.ListProductsRequest
import ru.k.kbook_api.grpc.product.ListProductsResponse
import ru.k.kbook_api.grpc.product.ProductResponse
import ru.k.kbook_api.grpc.product.ProductServiceGrpcKt
import ru.k.kbook_api.grpc.product.SortFieldDto
import ru.k.kbook_api.grpc.product.UpdateProductRequest
import ru.k.kbook_api.mapper.mergeWith
import ru.k.kbook_api.mapper.toProduct
import ru.k.kbook_api.mapper.toProductDto
import ru.k.kbook_api.service.ProductService

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
        } catch (e: Exception) {
            DeleteProductResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Failed to delete product: ${e.message}")
                .addUsedInDishes("Example dish name") // заглушка — в реальности можно проверять связи
                .build()
        }
    }

    override suspend fun listProducts(request: ListProductsRequest): ListProductsResponse {
        return try {
            val products = productService.getAllProducts(
                page = request.offset / (request.limit.coerceAtLeast(1)),
                size = request.limit.coerceAtLeast(1),
                sort = request.sortByField()
            ).map { it.toProductDto() }

            ListProductsResponse.newBuilder()
                .addAllProducts(products)
                .setTotalCount(products.size.toLong())
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
}

private fun ListProductsRequest.sortByField(): String = when (sortBy) {
    SortFieldDto.NAME -> "name"
    SortFieldDto.CALORICITY -> "caloricity"
    SortFieldDto.PROTEIN -> "protein"
    SortFieldDto.FAT -> "fat"
    SortFieldDto.CARB -> "carb"
    else -> "createdAt"
}
