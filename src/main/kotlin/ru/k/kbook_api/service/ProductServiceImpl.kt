package ru.k.kbook_api.service

import jakarta.persistence.EntityNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.k.kbook_api.mapper.toProduct
import ru.k.kbook_api.mapper.toProductDbo
import ru.k.kbook_api.repository.DishRepository
import ru.k.kbook_api.repository.ProductRepository
import ru.k.kbook_api.service.model.product.Product

@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val dishRepository: DishRepository,
) : ProductService {

    @Transactional(readOnly = true)
    override suspend fun getProductById(id: Long): Product = withContext(Dispatchers.IO) {
        productRepository.findByIdOrNull(id)
            ?.toProduct()
            ?: throw EntityNotFoundException("Product with id $id not found")
    }

    @Transactional(readOnly = true)
    override suspend fun getAllProducts(): List<Product> =
        withContext(Dispatchers.IO) {
            productRepository.findAll().map { it.toProduct() }
        }

    @Transactional
    override suspend fun createProduct(product: Product): Product = withContext(Dispatchers.IO) {
        validateProduct(product)
        val productDbo = product.copy(id = null).toProductDbo()
        val savedProduct = productRepository.save(productDbo)
        savedProduct.toProduct()
    }

    @Transactional
    override suspend fun updateProduct(id: Long, updatedProduct: Product): Product = withContext(Dispatchers.IO) {
        val existingProduct = productRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Product with id $id not found")

        validateProduct(updatedProduct.copy(id = id))
        val productDbo = updatedProduct.copy(id = id).toProductDbo()

        val savedProduct = productRepository.save(productDbo)
        savedProduct.toProduct()
    }

    @Transactional
    override suspend fun deleteProduct(id: Long) = withContext(Dispatchers.IO) {
        if (!productRepository.existsById(id)) {
            throw EntityNotFoundException("Product with id $id not found")
        }
        val usedIn = dishRepository.findDishNamesUsingProduct(id)
        if (usedIn.isNotEmpty()) {
            throw ProductInUseException(usedIn)
        }
        productRepository.deleteById(id)
    }

    private fun validateProduct(product: Product) {
        require(product.name.length >= 2) { "Название продукта минимум 2 символа" }
        require(product.images.size <= 5) { "Не более 5 фотографий продукта" }
        require(product.isNutritionValid) { "Сумма БЖУ на 100 г не может превышать 100" }
    }
}
