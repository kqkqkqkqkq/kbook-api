package ru.k.kbook_api.service

import io.qameta.allure.Description;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import org.junit.jupiter.api.DisplayName;
import io.kotest.core.spec.style.AnnotationSpec.AfterEach
import io.kotest.core.spec.style.AnnotationSpec.BeforeEach
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import io.qameta.allure.SeverityLevel
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.data.repository.findByIdOrNull
import ru.k.kbook_api.mapper.toProductDbo
import ru.k.kbook_api.repository.ProductRepository
import ru.k.kbook_api.repository.entity.product.ProductDbo
import ru.k.kbook_api.service.model.dish.DishProduct
import ru.k.kbook_api.service.model.product.CookingRequired
import ru.k.kbook_api.service.model.product.Product
import ru.k.kbook_api.service.model.product.ProductCategory
import java.util.stream.Stream
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

const val owner = "K"

class DishServiceTest {

    private val productRepository = mockk<ProductRepository>(relaxed = true)
    private val dishService = DishServiceImpl(
        dishRepository = mockk(relaxed = true),
        productRepository = productRepository,
    )

    @BeforeEach
    fun beforeEach() {

    }

    @AfterEach
    fun afterEach() {
        clearAllMocks()
    }

    @Test
    @Ignore
    @DisplayName("Сломанный тест")
    @Description("Для проверки allure отчета")
    @Severity(SeverityLevel.NORMAL)
    @Owner(owner)
    fun `SHOULD BE IGNORED`() {
        assertEquals(1, 1)
    }

    @Test
    @DisplayName("Пустой состав")
    @Description("При пустом составе блюда должно возвращаться нулевое значение КБЖУ")
    @Severity(SeverityLevel.CRITICAL)
    @Owner(owner)
    fun `GIVEN empty composition WHEN call calculateKbju THEN return zero kbju`() = runTest {
        val composition = emptyList<DishProduct>()

        val result = dishService.calculateKbju(composition)
        val expected = Kbju(0.0, 0.0, 0.0, 0.0)
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("Передан несуществующий продукт")
    @Description("В ситуации, когда в списке содержится несуществующий продукт будет выброшен IllegalArgumentException")
    @Severity(SeverityLevel.NORMAL)
    @Owner(owner)
    fun `GIVEN non existing product WHEN call calculateKbju THEN throw IllegalArgumentException`() = runTest {
        val id = 91282L
        val composition = listOf(DishProduct(id, null, 1489.0))
        coEvery { productRepository.findByIdOrNull(id) } returns null
        assertThrows<IllegalArgumentException> {
            val result = dishService.calculateKbju(composition)
        }
    }

    @Test
    @DisplayName("Состав из 3 продуктов")
    @Description("При составе из нескольких продуктов КБЖУ корректно расчитывается")
    @Severity(SeverityLevel.CRITICAL)
    @Owner(owner)
    fun `GIVEN 3 items composition WHEN call calculateKbju THEN return correct kbju`() = runTest {
        coEvery { productRepository.findByIdOrNull(chicken.id!!) } returns chicken
        coEvery { productRepository.findByIdOrNull(rice.id!!) } returns rice
        coEvery { productRepository.findByIdOrNull(avocado.id!!) } returns avocado

        val composition = listOf(
            DishProduct(chicken.id!!, null, 150.0), // 150г курицы
            DishProduct(rice.id!!, null, 100.0),     // 100г риса
            DishProduct(avocado.id!!, null, 50.0),   // 50г авокадо
        )

        val result = dishService.calculateKbju(composition)

        val expectedCal = 150.0 * 1.5 + 120.0 * 1.0 + 160.0 * 0.5 // = 225 + 120 + 80 = 425
        val expectedProt = 30.0 * 1.5 + 4.0 * 1.0 + 2.0 * 0.5     // = 45 + 4 + 1 = 50
        val expectedFat = 5.0 * 1.5 + 0.5 * 1.0 + 15.0 * 0.5      // = 7.5 + 0.5 + 7.5 = 15.5
        val expectedCarb = 0.0 * 1.5 + 28.0 * 1.0 + 9.0 * 0.5     // = 0 + 28 + 4.5 = 32.5

        assertEquals(Kbju(expectedCal, expectedProt, expectedFat, expectedCarb), result)
    }


    @ParameterizedTest
    @MethodSource("kbjuTestData")
    @DisplayName("Параметризованный тест для расчета КБЖУ блюда, где в составе 1 продукт")
    @Severity(SeverityLevel.CRITICAL)
    @Owner(owner)
    fun `GIVEN quantity and portion size WHEN call calculateKbju THEN return correct kbju`(
        product: ProductDbo,
        quantity: Double,
        expected: Kbju,
    ) = runTest {
        coEvery { productRepository.findByIdOrNull(product.id!!) } returns product

        val composition = listOf(DishProduct(product.id!!, null, quantity))
        val result = dishService.calculateKbju(composition)

        assertEquals(expected, result)
    }

    private companion object {

        @JvmStatic
        fun kbjuTestData(): Stream<Arguments> = Stream.of(
            Arguments.of(chicken, 100.0, Kbju(150.0, 30.0, 5.0, 0.0)),
            Arguments.of(chicken, 200.0, Kbju(300.0, 60.0, 10.0, 0.0)),
            Arguments.of(chicken, 300.0, Kbju(450.0, 90.0, 15.0, 0.0)),
        )

        val chicken = Product(
            id = 1,
            name = "Куриная грудка",
            caloricity = 150.0,
            protein = 30.0,
            fat = 5.0,
            carb = 0.0,
            category = ProductCategory.MEAT,
            cookingRequired = CookingRequired.REQUIRES_COOKING
        ).toProductDbo()

        val rice = Product(
            id = 2,
            name = "Бурый рис",
            caloricity = 120.0,
            protein = 4.0,
            fat = 0.5,
            carb = 28.0,
            category = ProductCategory.CEREALS,
            cookingRequired = CookingRequired.REQUIRES_COOKING
        ).toProductDbo()

        val avocado = Product(
            id = 3,
            name = "Авокадо",
            caloricity = 160.0,
            protein = 2.0,
            fat = 15.0,
            carb = 9.0,
            category = ProductCategory.VEGETABLES,
            cookingRequired = CookingRequired.READY_TO_EAT
        ).toProductDbo()

        val eggs = Product(
            id = 5,
            name = "Куриные яйца",
            caloricity = 155.0,
            protein = 12.6,
            fat = 11.0,
            carb = 1.1,
            category = ProductCategory.FROZEN,
            cookingRequired = CookingRequired.REQUIRES_COOKING
        ).toProductDbo()

        val products = listOf(chicken, rice, avocado, eggs)
    }
}
