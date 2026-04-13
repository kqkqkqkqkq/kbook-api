package ru.k.kbook_api.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import jakarta.persistence.EntityNotFoundException
import kotlinx.coroutines.test.runTest
import org.springframework.data.repository.findByIdOrNull
import ru.k.kbook_api.mapper.toProduct
import ru.k.kbook_api.mapper.toProductDbo
import ru.k.kbook_api.repository.DishRepository
import ru.k.kbook_api.repository.ProductRepository
import ru.k.kbook_api.repository.entity.product.CookingRequiredDbo
import ru.k.kbook_api.repository.entity.product.ProductCategoryDbo
import ru.k.kbook_api.repository.entity.product.ProductDbo
import ru.k.kbook_api.repository.entity.product.ProductFlagDbo
import ru.k.kbook_api.service.model.product.ContentType
import ru.k.kbook_api.service.model.product.CookingRequired
import ru.k.kbook_api.service.model.product.Product
import ru.k.kbook_api.service.model.product.ProductCategory
import ru.k.kbook_api.service.model.product.ProductFlag
import ru.k.kbook_api.service.model.product.ProductImage
import java.time.LocalDateTime

class ProductServiceImplTest : FunSpec({}) {

    private val productRepository = mockk<ProductRepository>()
    private val dishRepository = mockk<DishRepository>()
    private lateinit var service: ProductService

    init {
        beforeEach {
            service = ProductServiceImpl(productRepository, dishRepository)
        }

        afterEach {
            clearAllMocks()
        }

        context("GIVEN valid product id") {
            test("WHEN product exists THEN return product") {
                runTest {
                    // GIVEN
                    val id = 1L
                    val dbo = ProductDbo(
                        id = id,
                        name = "Apple",
                        images = mutableListOf(),
                        caloricity = 52.0,
                        protein = 0.3,
                        fat = 0.4,
                        carb = 14.0,
                        description = null,
                        category = ProductCategoryDbo.VEGETABLES,
                        cookingRequired = CookingRequiredDbo.READY_TO_EAT,
                        flags = mutableSetOf(ProductFlagDbo.VEGAN),
                        createdAt = LocalDateTime.now(),
                        updatedAt = null
                    )
                    coEvery { productRepository.findByIdOrNull(id) } returns dbo

                    // WHEN
                    val result = service.getProductById(id)

                    // THEN
                    result.name shouldBe "Apple"
                }
            }
        }

        context("GIVEN non-existent product id") {
            test("WHEN product not found THEN throw EntityNotFoundException") {
                runTest {
                    // GIVEN
                    val id = 999L
                    coEvery { productRepository.findByIdOrNull(id) } returns null

                    // WHEN & THEN
                    shouldThrow<EntityNotFoundException> {
                        service.getProductById(id)
                    }
                }
            }
        }

        context("GIVEN product with name shorter than 2 characters") {
            test("WHEN creating product THEN throw IllegalArgumentException") {
                runTest {
                    // GIVEN
                    val invalidProduct = Product(
                        id = null,
                        name = "A",
                        images = mutableListOf(),
                        caloricity = 50.0,
                        protein = 1.0,
                        fat = 1.0,
                        carb = 1.0,
                        description = null,
                        category = ProductCategory.VEGETABLES,
                        cookingRequired = CookingRequired.READY_TO_EAT,
                        flags = mutableSetOf(ProductFlag.VEGAN),
                        createdAt = null,
                        updatedAt = null
                    )

                    // WHEN & THEN
                    shouldThrow<IllegalArgumentException> {
                        service.createProduct(invalidProduct)
                    }
                }
            }
        }

        context("GIVEN product with more than 5 images") {
            test("WHEN creating product THEN throw IllegalArgumentException") {
                runTest {
                    // GIVEN
                    val invalidProduct = Product(
                        id = null,
                        name = "Banana",
                        images = (1..6).map {
                            ProductImage(
                                id = it.toLong(),
                                productId = null,
                                url = "http://example.com/image$it.png",
                                image = null,
                                contentType = ContentType.URL
                            )
                        },
                        caloricity = 89.0,
                        protein = 1.1,
                        fat = 0.3,
                        carb = 23.0,
                        description = null,
                        category = ProductCategory.VEGETABLES,
                        cookingRequired = CookingRequired.READY_TO_EAT,
                        flags = emptySet(),
                        createdAt = null,
                        updatedAt = null
                    )

                    // WHEN & THEN
                    shouldThrow<IllegalArgumentException> {
                        service.createProduct(invalidProduct)
                    }
                }
            }
        }

        context("GIVEN product with sum of BJJU > 100") {
            test("WHEN creating product THEN throw IllegalArgumentException") {
                runTest {
                    // GIVEN
                    val invalidProduct = Product(
                        id = null,
                        name = "Invalid Food",
                        images = listOf(),
                        caloricity = 500.0,
                        protein = 40.0,
                        fat = 40.0,
                        carb = 40.0, // сумма = 120
                        description = null,
                        category = ProductCategory.VEGETABLES,
                        cookingRequired = CookingRequired.READY_TO_EAT,
                        flags = emptySet(),
                        createdAt = null,
                        updatedAt = null
                    )

                    // WHEN & THEN
                    shouldThrow<IllegalArgumentException> {
                        service.createProduct(invalidProduct)
                    }
                }
            }
        }

        context("GIVEN valid product") {
            test("WHEN creating product THEN save and return product") {
                runTest {
                    // GIVEN
                    val product = Product(
                        id = null,
                        name = "Orange",
                        images = listOf(
                            ProductImage(
                                id = 1L,
                                productId = null,
                                url = "http://example.com/image.png",
                                image = null,
                                contentType = ContentType.URL
                            )
                        ),
                        caloricity = 47.0,
                        protein = 0.9,
                        fat = 0.1,
                        carb = 11.8,
                        description = null,
                        category = ProductCategory.VEGETABLES,
                        cookingRequired = CookingRequired.READY_TO_EAT,
                        flags = setOf(ProductFlag.VEGAN),
                        createdAt = null,
                        updatedAt = null
                    )

                    val savedDbo = product.copy(id = 1).toProductDbo()
                    coEvery { productRepository.save(any()) } returns savedDbo

                    // WHEN
                    val result = service.createProduct(product)

                    // THEN
                    result.id shouldBe 1
                }
            }
        }

        context("GIVEN product used in dishes") {
            test("WHEN deleting product that is used THEN throw ProductInUseException") {
                runTest {
                    // GIVEN
                    val id = 1L
                    coEvery { productRepository.existsById(id) } returns true
                    coEvery { dishRepository.findDishNamesUsingProduct(id) } returns listOf("Salad", "Smoothie")

                    // WHEN & THEN
                    val exception = shouldThrow<ProductInUseException> {
                        service.deleteProduct(id)
                    }
                    exception.dishNames shouldBe listOf("Salad", "Smoothie")
                }
            }
        }

        context("GIVEN product not used in any dish") {
            test("WHEN deleting product THEN delete successfully") {
                runTest {
                    // GIVEN
                    val id = 1L
                    coEvery { productRepository.existsById(id) } returns true
                    coEvery { dishRepository.findDishNamesUsingProduct(id) } returns emptyList()
                    coEvery { productRepository.deleteById(id) } just runs

                    // WHEN
                    service.deleteProduct(id)

                    // THEN (no exception thrown)
                    // Test passes if no exception
                }
            }
        }

        context("GIVEN existing product with valid updates") {
            test("WHEN updating product THEN persist changes") {
                runTest {
                    // GIVEN
                    val id = 1L
                    val existingDbo = ProductDbo(
                        id = id,
                        name = "Old Name",
                        images = mutableListOf(),
                        caloricity = 50.0,
                        protein = 1.0,
                        fat = 1.0,
                        carb = 1.0,
                        description = null,
                        category = ProductCategoryDbo.VEGETABLES,
                        cookingRequired = CookingRequiredDbo.READY_TO_EAT,
                        flags = mutableSetOf(),
                        createdAt = LocalDateTime.now(),
                        updatedAt = null
                    )
                    val updatedProduct = existingDbo.copy(name = "Updated Apple").toProduct()

                    coEvery { productRepository.findByIdOrNull(id) } returns existingDbo
                    coEvery { productRepository.save(any()) } returns updatedProduct.toProductDbo()

                    // WHEN
                    val result = service.updateProduct(id, updatedProduct)

                    // THEN
                    result.name shouldBe "Updated Apple"
                }
            }
        }

        context("GIVEN non-existent product id for update") {
            test("WHEN updating non-existent product THEN throw EntityNotFoundException") {
                runTest {
                    // GIVEN
                    val id = 999L
                    val product = Product(
                        id = id,
                        name = "Test",
                        images = listOf(),
                        caloricity = 50.0,
                        protein = 1.0,
                        fat = 1.0,
                        carb = 1.0,
                        description = null,
                        category = ProductCategory.VEGETABLES,
                        cookingRequired = CookingRequired.READY_TO_EAT,
                        flags = emptySet(),
                        createdAt = null,
                        updatedAt = null
                    )
                    coEvery { productRepository.findByIdOrNull(id) } returns null

                    // WHEN & THEN
                    shouldThrow<EntityNotFoundException> {
                        service.updateProduct(id, product)
                    }
                }
            }
        }
    }
}
