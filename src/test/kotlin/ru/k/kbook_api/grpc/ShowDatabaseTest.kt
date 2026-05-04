package ru.k.kbook_api.grpc

import kotlinx.coroutines.test.runTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("test")
class ShowDatabaseTest(
    @Autowired private val service: ProductGrpcService,
) {

//    @Test
//    fun `SHOW DATABASE ADD VALUE`() = runTest {
//        val name = "Test product"
//        val product = service.createProduct(ProductModelBuilder.createProductRequest(name)).product
//        val result = service.getProduct(ProductModelBuilder.getProductRequest(product.id)).product
//        assertEquals(name, result.name)
//    }
//
//    @Test
//    fun `SHOW DATABASE COLLECT VALUES`() = runTest {
//        val name = "Test product"
//        val request = ProductModelBuilder.listProductsRequest()
//        val products = service.listProducts(request).productsList
//        assertEquals(name, products.first().name)
//    }

}