package ru.k.kbook_api.service

import ru.k.kbook_api.service.model.product.Product

interface ProductService {
    suspend fun getProductById(id: Long): Product
    suspend fun getAllProducts(): List<Product>
    suspend fun createProduct(product: Product): Product
    suspend fun updateProduct(id: Long, updatedProduct: Product): Product
    suspend fun deleteProduct(id: Long)
}
