package ru.k.kbook_api.service

import jakarta.persistence.EntityNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.k.kbook_api.mapper.toProduct
import ru.k.kbook_api.mapper.toProductDbo
import ru.k.kbook_api.repository.ProductRepository
import ru.k.kbook_api.service.model.Product

@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository,
) : ProductService {

    @Transactional(readOnly = true)
    override suspend fun getProductById(id: Long): Product = withContext(Dispatchers.IO) {
        productRepository.findByIdOrNull(id)
            ?.toProduct()
            ?: throw EntityNotFoundException("Product with id $id not found")
    }

    @Transactional(readOnly = true)
    override suspend fun getAllProducts(page: Int, size: Int, sort: String): List<Product> =
        withContext(Dispatchers.IO) {
            val pageRequest = PageRequest.of(page, size, Sort.by(sort))
            productRepository.findAll(pageRequest).content.map { it.toProduct() }
        }

    @Transactional
    override suspend fun createProduct(product: Product): Product = withContext(Dispatchers.IO) {
        val productDbo = product.copy(id = null).toProductDbo()
        val savedProduct = productRepository.save(productDbo)
        savedProduct.toProduct()
    }

    @Transactional
    override suspend fun updateProduct(id: Long, updatedProduct: Product): Product = withContext(Dispatchers.IO) {
        val existingProduct = productRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Product with id $id not found")

        val productDbo = updatedProduct.copy(id = id).toProductDbo()

        val savedProduct = productRepository.save(productDbo)
        savedProduct.toProduct()
    }

    @Transactional
    override suspend fun deleteProduct(id: Long) = withContext(Dispatchers.IO) {
        if (!productRepository.existsById(id)) {
            throw EntityNotFoundException("Product with id $id not found")
        }
        productRepository.deleteById(id)
    }
}
