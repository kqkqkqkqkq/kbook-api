package ru.k.kbook_api.grpc

import io.kotest.core.spec.style.AnnotationSpec.BeforeEach
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import ru.k.kbook_api.grpc.product.DeleteProductRequest
import ru.k.kbook_api.grpc.product.ProductCategoryDto
import ru.k.kbook_api.repository.ProductRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Интеграционные тесты gRPC сервиса продуктов")
class ProductGrpcServiceTest(
    @Autowired private val service: ProductGrpcService,
    @Autowired private val productRepository: ProductRepository,
) {

    @BeforeEach
    fun beforeEach() {
        productRepository.deleteAll()
    }

    @Test
    fun `GIVEN product name is empty WHEN call createProduct THEN return success false`() = runTest {
        val request = ProductModelBuilder.createProductRequest("")
        val response = service.createProduct(request)
        assertEquals(false, response.success)
    }

    @Test
    fun `GIVEN product name has one symbol WHEN call createProduct THEN return success false`() = runTest {
        val request = ProductModelBuilder.createProductRequest("A")
        val response = service.createProduct(request)
        assertEquals(false, response.success)
    }

    @Test
    fun `GIVEN valid product creation request WHEN call createProduct THEN return success true`() = runTest {
        val request = ProductModelBuilder.createProductRequest("Apple")
        val response = service.createProduct(request)
        assertEquals(true, response.success)
    }

    @Test
    fun `GIVEN non-existent product id WHEN call getProduct THEN return success false`() = runTest {
        val request = ProductModelBuilder.getProductRequest(1)
        val response = service.getProduct(request)
        assertEquals(false, response.success)
    }

    @Test
    fun `GIVEN existing product WHEN call getProduct THEN return product with correct name`() = runTest {
        val createRequest = ProductModelBuilder.createProductRequest("Apple")
        val created = service.createProduct(createRequest)
        val getRequest = ProductModelBuilder.getProductRequest(created.product.id)
        val retrieved = service.getProduct(getRequest)
        assertEquals("Apple", retrieved.product.name)
    }

    @Test
    fun `GIVEN existing product WHEN call updateProduct THEN return updated product name`() = runTest {
        val createRequest = ProductModelBuilder.createProductRequest("Old Name")
        val created = service.createProduct(createRequest)
        val updateRequest = ProductModelBuilder.updateProductRequest(created.product.id, "New Name")
        val response = service.updateProduct(updateRequest)
        assertEquals("New Name", response.product.name)
    }

    @Test
    fun `GIVEN non-existent product id WHEN call updateProduct THEN return success false`() = runTest {
        val updateRequest = ProductModelBuilder.updateProductRequest(999, "Non-existent")
        val response = service.updateProduct(updateRequest)
        assertEquals(false, response.success)
    }

    @Test
    fun `GIVEN existing product WHEN call deleteProduct THEN return success true`() = runTest {
        val createRequest = ProductModelBuilder.createProductRequest("To Delete")
        val created = service.createProduct(createRequest)
        val deleteRequest = ProductModelBuilder.deleteProductRequest(created.product.id)
        val response = service.deleteProduct(deleteRequest)
        assertEquals(true, response.success)
    }

    @Test
    fun `GIVEN multiple products created WHEN call listProducts THEN return total count of all products`() = runTest {
        service.createProduct(ProductModelBuilder.createProductRequest("Apple"))
        service.createProduct(ProductModelBuilder.createProductRequest("Banana"))
        val request = ProductModelBuilder.listProductsRequest()
        val response = service.listProducts(request)
        assertEquals(2, response.totalCount.toInt())
    }

    @Test
    fun `GIVEN products with different names WHEN call listProducts with search query THEN return filtered count`() = runTest {
        service.createProduct(ProductModelBuilder.createProductRequest("Apple Juice"))
        service.createProduct(ProductModelBuilder.createProductRequest("Orange Juice"))
        val request = ProductModelBuilder.listProductsRequest().toBuilder()
            .setSearchQuery("apple")
            .build()
        val response = service.listProducts(request)
        assertEquals(1, response.productsCount)
    }

    @Test
    fun `GIVEN products in different categories WHEN call listProducts with category filter THEN return matching products`() = runTest {
        service.createProduct(
            ProductModelBuilder.createProductRequest("Chicken")
                .toBuilder()
                .setCategory(ProductCategoryDto.MEAT)
                .build()
        )
        service.createProduct(
            ProductModelBuilder.createProductRequest("Lettuce")
                .toBuilder()
                .setCategory(ProductCategoryDto.VEGETABLES)
                .build()
        )
        val request = ProductModelBuilder.listProductsRequest().toBuilder()
            .addCategories(ProductCategoryDto.MEAT)
            .build()
        val response = service.listProducts(request)
        assertEquals(1, response.productsCount)
        assertEquals("Chicken", response.productsList.first().name)
    }

    @Test
    fun `GIVEN products WHEN call listProducts with sort by name ascending THEN return alphabetically ordered`() = runTest {
        service.createProduct(ProductModelBuilder.createProductRequest("Zucchini"))
        service.createProduct(ProductModelBuilder.createProductRequest("Apple"))
        val request = ProductModelBuilder.listProductsRequest().toBuilder()
            .setSortBy(ru.k.kbook_api.grpc.product.SortFieldDto.NAME)
            .setSortDirection(ru.k.kbook_api.grpc.product.SortDirectionDto.ASC)
            .build()
        val response = service.listProducts(request)
        assertEquals("Apple", response.productsList.first().name)
    }

    @Test
    fun `GIVEN products WHEN call listProducts with sort by name descending THEN return reverse alphabetically ordered`() = runTest {
        service.createProduct(ProductModelBuilder.createProductRequest("Apple"))
        service.createProduct(ProductModelBuilder.createProductRequest("Zucchini"))
        val request = ProductModelBuilder.listProductsRequest().toBuilder()
            .setSortBy(ru.k.kbook_api.grpc.product.SortFieldDto.NAME)
            .setSortDirection(ru.k.kbook_api.grpc.product.SortDirectionDto.DESC)
            .build()
        val response = service.listProducts(request)
        assertEquals("Zucchini", response.productsList.first().name)
    }

    @Test
    fun `GIVEN product ids WHEN call getProductsForDish THEN return list with same size as input`() = runTest {
        val p1 = service.createProduct(ProductModelBuilder.createProductRequest("Milk")).product
        val p2 = service.createProduct(ProductModelBuilder.createProductRequest("Sugar")).product
        val request = ProductModelBuilder.getProductsForDishRequest(listOf(p1.id, p2.id))
        val response = service.getProductsForDish(request)
        assertEquals(2, response.productsCount)
    }

    @Test
    fun `GIVEN product with more than 5 images WHEN call createProduct THEN return success false`() = runTest {
        val images = (1..6).map { ProductModelBuilder.imageInput("url-$it") }
        val request = ProductModelBuilder.createProductRequest("Too Many Images")
            .toBuilder()
            .clearImages()
            .addAllImages(images)
            .build()
        val response = service.createProduct(request)
        assertEquals(false, response.success)
    }

    @Test
    fun `GIVEN product with invalid nutrition WHEN call createProduct THEN return success false`() = runTest {
        val request = ProductModelBuilder.createProductRequest("Invalid Nutrition")
            .toBuilder()
            .setProtein(40.0)
            .setFat(40.0)
            .setCarb(40.0)
            .build()
        val response = service.createProduct(request)
        assertEquals(false, response.success)
    }

    @Test
    fun `GIVEN product WHEN call deleteProduct THEN return success`() = runTest {
        val request = ProductModelBuilder.createProductRequest("Apple")
        val response = service.createProduct(request)
        val deleteResponse = service.deleteProduct(DeleteProductRequest.newBuilder().setId(response.product.id).build())
        val list = service.listProducts(ProductModelBuilder.listProductsRequest())
        assertTrue(list.productsList.isEmpty())
    }
}
