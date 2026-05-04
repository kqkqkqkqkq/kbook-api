package ru.k.kbook_api.grpc

import io.grpc.StatusException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import ru.k.kbook_api.grpc.dish.DishCategory
import ru.k.kbook_api.grpc.product.ProductDto
import ru.k.kbook_api.repository.DishRepository
import ru.k.kbook_api.repository.ProductRepository
import ru.k.kbook_api.service.model.product.Product
import kotlin.code
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Интеграционные тесты gRPC сервиса блюд")
class DishGrpcServiceTest(
    @Autowired private val dishService: DishGrpcService,
    @Autowired private val productService: ProductGrpcService,
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val dishRepository: DishRepository,
) {

    @BeforeEach
    fun beforeEach() {
        dishRepository.deleteAll()
        productRepository.deleteAll()
    }

    @Test
    fun `GIVEN dish name is one symbol WHEN call createDish THEN throw StatusException`() = runTest {
        val request = DishModelBuilder.createDishRequest(name = "A")
        assertFailsWith<StatusException> { dishService.createDish(request) }
    }

    @Test
    fun `GIVEN dish name is empty WHEN call createDish THEN throw StatusException`() = runTest {
        val request = DishModelBuilder.createDishRequest(name = "")
        assertFailsWith<StatusException> { dishService.createDish(request) }
    }

    @Test
    fun `GIVEN more than 5 images WHEN call createDish THEN throw StatusException`() = runTest {
        val images = (1..6).map { DishModelBuilder.dishImage(url = "url-$it") }
        val composition = listOf(DishModelBuilder.dishProduct(productId = createTestProduct().id))
        val request = DishModelBuilder.createDishRequestWithComposition(composition = composition)
            .toBuilder()
            .addAllImages(images)
            .build()
        assertFailsWith<StatusException> { dishService.createDish(request) }
    }

    @Test
    fun `GIVEN empty composition WHEN call createDish THEN throw StatusException`() = runTest {
        val request = DishModelBuilder.createDishRequest()
        assertFailsWith<StatusException> { dishService.createDish(request) }
    }

    @Test
    fun `GIVEN portion size zero WHEN call createDish THEN throw StatusException`() = runTest {
        val composition = listOf(DishModelBuilder.dishProduct(productId = createTestProduct().id))
        val request = DishModelBuilder.createDishRequestWithComposition(portionSize = 0.0, composition = composition)
        assertFailsWith<StatusException> { dishService.createDish(request) }
    }

    @Test
    fun `GIVEN negative quantity in composition WHEN call createDish THEN throw StatusException`() = runTest {
        val productId = createTestProduct().id
        val composition = listOf(DishModelBuilder.dishProduct(productId = productId, quantity = -10.0))
        val request = DishModelBuilder.createDishRequestWithComposition(composition = composition)
        assertFailsWith<StatusException> { dishService.createDish(request) }
    }

    @Test
    fun `GIVEN nonexistent product in composition WHEN call createDish THEN throw StatusException`() = runTest {
        val composition = listOf(DishModelBuilder.dishProduct(productId = 999L))
        val request = DishModelBuilder.createDishRequestWithComposition(composition = composition)
        assertFailsWith<StatusException> { dishService.createDish(request) }
    }

    @Test
    fun `GIVEN valid dish request with macro in name WHEN call createDish THEN return dish with stripped name and correct category`() = runTest {
        val productId = createTestProduct().id
        val composition = listOf(DishModelBuilder.dishProduct(productId = productId))
        val request = DishModelBuilder.createDishRequestWithComposition(
            name = "!второе Test Dish",
            composition = composition
        )
        val response = dishService.createDish(request)
        assertEquals("Test Dish", response.name)
    }

    @Test
    fun `GIVEN dish with invalid manual KBZHU WHEN call createDish THEN throw INVALID_ARGUMENT`() = runTest {
        val productId = createTestProduct().id
        val composition = listOf(DishModelBuilder.dishProduct(productId = productId, quantity = 100.0))
        val request = DishModelBuilder.createDishRequestWithComposition(composition = composition)
            .toBuilder()
            .setCaloricity(-100.0)
            .build()
        assertFailsWith<StatusException> { dishService.createDish(request) }
    }

    @Test
    fun `GIVEN bju sum exceeds 100g per portion WHEN call createDish THEN throw INVALID_ARGUMENT`() = runTest {
        val productId = createHighBjuProduct().id
        val composition = listOf(DishModelBuilder.dishProduct(productId = productId, quantity = 100.0))
        val request = DishModelBuilder.createDishRequestWithComposition(
            portionSize = 100.0,
            composition = composition
        )
        assertFailsWith<StatusException> { dishService.createDish(request) }
    }

    @Test
    fun `GIVEN unavailable flags in request WHEN call createDish THEN throw INVALID_ARGUMENT`() = runTest {
        val productId = createTestProduct().id
        val composition = listOf(DishModelBuilder.dishProduct(productId = productId))
        val request = DishModelBuilder.createDishRequestWithComposition(composition = composition)
            .toBuilder()
            .addFlags(ru.k.kbook_api.grpc.dish.DishFlag.VEGAN)
            .build()
        assertFailsWith<StatusException> { dishService.createDish(request) }
    }

    @Test
    fun `GIVEN valid dish request WHEN call createDish THEN persist dish`() = runTest {
        val productId = createTestProduct().id
        val composition = listOf(DishModelBuilder.dishProduct(productId = productId))
        val request = DishModelBuilder.createDishRequestWithComposition(composition = composition)
        val response = dishService.createDish(request)
        assertTrue(dishRepository.existsById(response.id))
    }

    @Test
    fun `GIVEN non-existent dish id WHEN call getDish THEN throw NOT_FOUND`() = runTest {
        val request = DishModelBuilder.getDishRequest(id = 999)
        assertFailsWith<StatusException> { dishService.getDish(request) }
    }

    @Test
    fun `GIVEN existing dish WHEN call getDish THEN return correct dish`() = runTest {
        val productId = createTestProduct().id
        val composition = listOf(DishModelBuilder.dishProduct(productId = productId))
        val created = dishService.createDish(DishModelBuilder.createDishRequestWithComposition(composition = composition))
        val request = DishModelBuilder.getDishRequest(id = created.id)
        val response = dishService.getDish(request)
        assertEquals(created.id, response.id)
    }

    @Test
    fun `GIVEN non-existent dish id WHEN call updateDish THEN throw NOT_FOUND`() = runTest {
        val request = DishModelBuilder.updateDishRequest(id = 999, name = "New Name")
        assertFailsWith<StatusException> { dishService.updateDish(request) }
    }

    @Test
    fun `GIVEN null composition in update request WHEN call updateDish THEN keep existing composition`() = runTest {
        val productId = createTestProduct().id
        val initialComposition = listOf(DishModelBuilder.dishProduct(productId = productId))
        val created = dishService.createDish(DishModelBuilder.createDishRequestWithComposition(composition = initialComposition))
        val updateRequest = DishModelBuilder.updateDishRequest(id = created.id, name = "Updated Name")
        val updated = dishService.updateDish(updateRequest)
        assertEquals("Updated Name", updated.name)
    }

    @Test
    fun `GIVEN empty composition in update request WHEN call updateDish THEN throw INVALID_ARGUMENT`() = runTest {
        val productId = createTestProduct().id
        val initialComposition = listOf(DishModelBuilder.dishProduct(productId = productId))
        val created = dishService.createDish(DishModelBuilder.createDishRequestWithComposition(composition = initialComposition))
        val updateRequest = DishModelBuilder.updateDishRequest(id = created.id)
            .toBuilder()
            .setComposition(ru.k.kbook_api.grpc.dish.DishComposition.getDefaultInstance())
            .build()
        assertFailsWith<StatusException> { dishService.updateDish(updateRequest) }
    }

    @Test
    fun `GIVEN valid update request with new composition WHEN call updateDish THEN update composition`() = runTest {
        val p1 = createTestProduct("Product 1").id
        val p2 = createTestProduct("Product 2").id
        val initialComposition = listOf(DishModelBuilder.dishProduct(productId = p1))
        val created = dishService.createDish(DishModelBuilder.createDishRequestWithComposition(composition = initialComposition))
        val newComposition = listOf(DishModelBuilder.dishProduct(productId = p2))
        val updateRequest = DishModelBuilder.updateDishRequest(id = created.id)
            .toBuilder()
            .setComposition(ru.k.kbook_api.grpc.dish.DishComposition.newBuilder().addAllItems(newComposition).build())
            .build()
        val updated = dishService.updateDish(updateRequest)
        assertEquals(p2, updated.compositionList.first().productId)
    }

    @Test
    fun `GIVEN non-existent dish id WHEN call deleteDish THEN throw NOT_FOUND`() = runTest {
        val request = DishModelBuilder.deleteDishRequest(id = 999)
        assertFailsWith<StatusException> { dishService.deleteDish(request) }
    }

    @Test
    fun `GIVEN existing dish WHEN call deleteDish THEN remove from repository`() = runTest {
        val productId = createTestProduct().id
        val composition = listOf(DishModelBuilder.dishProduct(productId = productId))
        val created = dishService.createDish(DishModelBuilder.createDishRequestWithComposition(composition = composition))
        val deleteRequest = DishModelBuilder.deleteDishRequest(id = created.id)
        dishService.deleteDish(deleteRequest)
        assertEquals(false, dishRepository.existsById(created.id))
    }

    @Test
    fun `GIVEN existing dishes WHEN call listDishes with search query THEN return filtered results`() = runTest {
        val productId = createTestProduct().id
        val composition = listOf(DishModelBuilder.dishProduct(productId = productId))
        dishService.createDish(DishModelBuilder.createDishRequestWithComposition(name = "Borscht"))
        dishService.createDish(DishModelBuilder.createDishRequestWithComposition(name = "Pancakes"))
        val request = DishModelBuilder.listDishesRequest(searchQuery = "bor")
        val response = dishService.listDishes(request)
        assertEquals("Borscht", response.dishesList.first().name)
    }

    @Test
    fun `GIVEN dishes with categories WHEN call listDishes with category filter THEN return matching dishes`() = runTest {
        val productId = createTestProduct().id
        val composition = listOf(DishModelBuilder.dishProduct(productId = productId))
        dishService.createDish(DishModelBuilder.createDishRequestWithComposition(category = DishCategory.SOUP))
        dishService.createDish(DishModelBuilder.createDishRequestWithComposition(category = DishCategory.SECOND))
        val request = DishModelBuilder.listDishesRequest(categories = listOf(DishCategory.SOUP))
        val response = dishService.listDishes(request)
        assertEquals(DishCategory.SOUP, response.dishesList.first().category)
    }

    @Test
    fun `GIVEN valid dish data WHEN call validateDish THEN return valid true`() = runTest {
        val productId = createTestProduct().id
        val composition = listOf(DishModelBuilder.dishProduct(productId = productId))
        val request = DishModelBuilder.createDishRequestWithComposition(composition = composition)
        val response = dishService.validateDish(request)
        assertTrue(response.valid)
    }

    @Test
    fun `GIVEN invalid dish data WHEN call validateDish THEN return valid false with errors`() = runTest {
        val request = DishModelBuilder.createDishRequest(name = "")
        val response = dishService.validateDish(request)
        assertEquals(false, response.valid)
    }

    @Test
    fun `GIVEN product is used in dish WHEN call deleteProduct THEN return failure with dish names`() = runTest {
        val productId = createTestProduct("Used Apple").id
        val composition = listOf(DishModelBuilder.dishProduct(productId = productId))
        dishService.createDish(DishModelBuilder.createDishRequestWithComposition(name = "Borscht", composition = composition))

        val deleteRequest = ProductModelBuilder.deleteProductRequest(id = productId)
        val result = productService.deleteProduct(deleteRequest)

        assertEquals("Borscht", result.usedInDishesList.first())
    }

    private suspend fun createTestProduct(name: String = "Test Product"): ProductDto {
        val request = ProductModelBuilder.createProductRequest(name)
        return productService.createProduct(request).product
    }

    private suspend fun createHighBjuProduct(): ProductDto {
        val request = ProductModelBuilder.createProductRequest("High Bju Product")
            .toBuilder()
            .setProtein(40.0)
            .setFat(40.0)
            .setCarb(40.0)
            .build()
        return productService.createProduct(request).product
    }

}
