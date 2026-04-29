package ru.k.kbook_api.grpc

import io.kotest.core.spec.style.AnnotationSpec.BeforeEach
import kotlinx.coroutines.test.runTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import ru.k.kbook_api.grpc.product.CookingRequiredDto
import ru.k.kbook_api.grpc.product.CreateProductRequest
import ru.k.kbook_api.grpc.product.GetProductRequest
import ru.k.kbook_api.grpc.product.ListProductsRequest
import ru.k.kbook_api.grpc.product.ProductCategoryDto
import ru.k.kbook_api.grpc.product.ProductResponse
import kotlin.collections.emptyList
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("test")
class ProductGrpcServiceTest(
    @Autowired private val service: ProductGrpcService,
) {

    @BeforeEach
    fun beforeEach() {

    }

    @Test
    fun `GIVEN WHEN THEN`() = runTest {
        val productRequest = getProductRequest(10)
        val response = ProductResponse.newBuilder()
            .setSuccess(false)
            .build()

        val result = service.getProduct(productRequest)
        assertEquals(response.success, result.success)
    }

    @Test
    fun `GIVEN WHEN WHEN THEN get product`() = runTest {
        val createRequest = createProductRequest("name")
        val product = service.createProduct(createRequest).product
        val getRequest = getProductRequest(product.id)
        val existingProduct = service.getProduct(getRequest)
        assertEquals(product.name, existingProduct.product.name)
    }

    @Test
    fun `GIVEN WHEN WHEN THEN get all`() = runTest {
        val request = listProductsRequest()
        val products = service.listProducts(request).productsList
        println(products.first())
        assertEquals(1, products.size)
    }

    private fun getProductRequest(id: Long) = GetProductRequest
        .newBuilder()
        .setId(id)
        .build()

    private fun createProductRequest(name: String) = CreateProductRequest
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

    private fun listProductsRequest() = ListProductsRequest
        .newBuilder()
        .build()
}
