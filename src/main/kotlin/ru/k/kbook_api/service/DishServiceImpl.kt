package ru.k.kbook_api.service

import jakarta.persistence.EntityNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.k.kbook_api.mapper.toDish
import ru.k.kbook_api.mapper.toDishCategory
import ru.k.kbook_api.mapper.toDishCategoryDbo
import ru.k.kbook_api.mapper.toDishFlag
import ru.k.kbook_api.mapper.toDishFlagDbo
import ru.k.kbook_api.mapper.toDishImageDbo
import ru.k.kbook_api.repository.DishRepository
import ru.k.kbook_api.repository.entity.dish.DishDbo
import ru.k.kbook_api.repository.entity.dish.DishProductDbo
import ru.k.kbook_api.repository.entity.product.ProductFlagDbo
import ru.k.kbook_api.repository.ProductRepository
import ru.k.kbook_api.service.model.dish.Dish
import ru.k.kbook_api.service.model.dish.DishCategory
import ru.k.kbook_api.service.model.dish.DishFlag
import ru.k.kbook_api.service.model.dish.DishProduct

@Service
class DishServiceImpl(
    private val dishRepository: DishRepository,
    private val productRepository: ProductRepository,
) : DishService {

    private data class MacroHit(val start: Int, val length: Int, val category: DishCategory)

    @Transactional
    override suspend fun createDish(request: CreateDishRequest): Dish = withContext(Dispatchers.IO) {
        validateDishCommon(
            name = request.name,
            imagesCount = request.images.size,
            composition = request.composition,
            portionSize = request.portionSize,
        )

        val macroHit = findFirstMacro(request.name)
        val categoryFromForm = request.category
        val category = categoryFromForm ?: macroHit?.category
            ?: throw IllegalArgumentException("Укажите категорию блюда или макрос категории в названии")

        val cleanName = macroHit?.let { stripMacro(request.name, it) }?.trim() ?: request.name.trim()
        require(cleanName.length >= 2) { "Название после обработки макроса должно быть не короче 2 символов" }

        val kbju = calculateKbju(request.composition, request.portionSize)
        assertBjuPer100g(kbju, request.portionSize)

        val available = availableFlagsForComposition(request.composition)
        val flags = request.flags.intersect(available)
        if (request.flags != flags) {
            throw IllegalArgumentException("Заданы флаги, недоступные для текущего состава блюда")
        }

        val dish = DishDbo(
            name = cleanName,
            images = mutableListOf(),
            caloricity = kbju.caloricity,
            protein = kbju.protein,
            fat = kbju.fat,
            carb = kbju.carb,
            composition = mutableListOf(),
            portionSize = request.portionSize,
            category = category.toDishCategoryDbo(),
            flags = flags.map { it.toDishFlagDbo() }.toMutableSet(),
        )

        request.images.forEach { img ->
            dish.images.add(img.toDishImageDbo(dish))
        }
        request.composition.forEach { line ->
            val productRef = productRepository.getReferenceById(line.productId)
            dish.composition.add(DishProductDbo(dish = dish, product = productRef, quantity = line.quantity))
        }

        dishRepository.save(dish).toDish()
    }

    @Transactional
    override suspend fun updateDish(id: Long, request: UpdateDishRequest): Dish = withContext(Dispatchers.IO) {
        val existing = dishRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Блюдо с id $id не найдено")

        if (request.images != null) {
            require(request.images.size <= 5) { "Не более 5 фотографий" }
            existing.images.clear()
            request.images.forEach { existing.images.add(it.toDishImageDbo(existing)) }
        }

        if (request.composition != null) {
            validateComposition(request.composition, request.portionSize ?: existing.portionSize)
            existing.composition.clear()
            request.composition.forEach { line ->
                val productRef = productRepository.getReferenceById(line.productId)
                existing.composition.add(DishProductDbo(dish = existing, product = productRef, quantity = line.quantity))
            }
        }

        request.portionSize?.let {
            require(it > 0) { "Размер порции должен быть больше 0" }
            existing.portionSize = it
        }

        var newName = existing.name
        var newCategory = existing.category.toDishCategory()

        request.category?.let { newCategory = it }

        request.name?.let { raw ->
            val macroHit = findFirstMacro(raw)
            newName = macroHit?.let { stripMacro(raw, it) }?.trim() ?: raw.trim()
            require(newName.length >= 2) { "Название после обработки макроса должно быть не короче 2 символов" }
            if (request.category == null && macroHit != null) {
                newCategory = macroHit.category
            }
        }

        existing.name = newName
        existing.category = newCategory.toDishCategoryDbo()

        val compositionForCalc = existing.toCompositionInputs()
        val portionForCalc = existing.portionSize
        val recalc = request.composition != null || request.portionSize != null
        if (recalc) {
            val kbju = calculateKbju(compositionForCalc, portionForCalc)
            assertBjuPer100g(kbju, portionForCalc)
            existing.caloricity = request.caloricity ?: kbju.caloricity
            existing.protein = request.protein ?: kbju.protein
            existing.fat = request.fat ?: kbju.fat
            existing.carb = request.carb ?: kbju.carb
        } else {
            request.caloricity?.let { existing.caloricity = it }
            request.protein?.let { existing.protein = it }
            request.fat?.let { existing.fat = it }
            request.carb?.let { existing.carb = it }
            assertBjuPer100g(
                Kbju(existing.caloricity, existing.protein, existing.fat, existing.carb),
                portionForCalc,
            )
        }

        val available = availableFlagsForComposition(compositionForCalc)
        val existingFlags = existing.flags.map { it.toDishFlag() }.toSet()
        val wantedFlags = request.flags ?: existingFlags
        val finalFlags = wantedFlags.intersect(available)
        existing.flags.clear()
        existing.flags.addAll(finalFlags.map { it.toDishFlagDbo() })

        dishRepository.save(existing).toDish()
    }

    @Transactional(readOnly = true)
    override suspend fun getDish(id: Long): Dish = withContext(Dispatchers.IO) {
        dishRepository.findByIdOrNull(id)?.toDish()
            ?: throw EntityNotFoundException("Блюдо с id $id не найдено")
    }

    @Transactional(readOnly = true)
    override suspend fun listDishes(filter: DishFilter): List<Dish> = withContext(Dispatchers.IO) {
        var list = dishRepository.findAllWithDetails().map { it.toDish() }
        filter.categories?.takeIf { it.isNotEmpty() }?.let { cats ->
            val set = cats.toSet()
            list = list.filter { it.category in set }
        }
        filter.flags?.takeIf { it.isNotEmpty() }?.let { req ->
            val required = req.toSet()
            list = list.filter { dish -> required.all { f -> f in dish.flags } }
        }
        filter.search?.takeIf { it.isNotBlank() }?.let { q ->
            val needle = q.trim().lowercase()
            list = list.filter { it.name.lowercase().contains(needle) }
        }
        list
    }

    @Transactional
    override suspend fun deleteDish(id: Long) = withContext(Dispatchers.IO) {
        if (!dishRepository.existsById(id)) {
            throw EntityNotFoundException("Блюдо с id $id не найдено")
        }
        dishRepository.deleteById(id)
    }

    override suspend fun validateDish(request: CreateDishRequest): ValidateDishResponse = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()
        runCatching {
            validateDishCommon(
                name = request.name,
                imagesCount = request.images.size,
                composition = request.composition,
                portionSize = request.portionSize,
            )
        }.onFailure { errors.add(it.message ?: it.toString()) }

        val categoryOk = request.category != null || findFirstMacro(request.name) != null
        if (!categoryOk) errors.add("Укажите категорию или макрос в названии")

        val kbju = runCatching { calculateKbju(request.composition, request.portionSize) }.getOrNull()
        if (kbju != null && request.portionSize > 0) {
            runCatching { assertBjuPer100g(kbju, request.portionSize) }.onFailure {
                errors.add(it.message ?: it.toString())
            }
        }

        val available = runCatching { availableFlagsForComposition(request.composition) }.getOrDefault(emptySet())
        if (request.flags.any { it !in available }) {
            errors.add("Недоступные флаги для текущего состава")
        }

        ValidateDishResponse(
            valid = errors.isEmpty(),
            errors = errors,
            calculatedKbju = kbju,
            availableFlags = available,
        )
    }

    override suspend fun calculateKbju(composition: List<DishProduct>, portionSize: Double): Kbju =
        withContext(Dispatchers.IO) {
            var cal = 0.0
            var prot = 0.0
            var fat = 0.0
            var carb = 0.0
            composition.forEach { line ->
                val product = productRepository.findById(line.productId).orElseThrow()
                val per100g = line.quantity / 100.0
                cal += product.caloricity * per100g
                prot += product.protein * per100g
                fat += product.fat * per100g
                carb += product.carb * per100g
            }
            Kbju(cal, prot, fat, carb)
        }

    override suspend fun getAvailableFlags(composition: List<DishProduct>): Set<DishFlag> =
        withContext(Dispatchers.IO) { availableFlagsForComposition(composition) }

    private fun availableFlagsForComposition(composition: List<DishProduct>): Set<DishFlag> {
        if (composition.isEmpty()) return emptySet()
        var vegan = true
        var gf = true
        var sf = true
        composition.forEach { line ->
            val p = productRepository.findById(line.productId).orElseThrow()
            if (ProductFlagDbo.VEGAN !in p.flags) vegan = false
            if (ProductFlagDbo.GLUTEN_FREE !in p.flags) gf = false
            if (ProductFlagDbo.SUGAR_FREE !in p.flags) sf = false
        }
        return buildSet {
            if (vegan) add(DishFlag.VEGAN)
            if (gf) add(DishFlag.GLUTEN_FREE)
            if (sf) add(DishFlag.SUGAR_FREE)
        }
    }

    private fun findFirstMacro(name: String): MacroHit? {
        val entries = listOf(
            "!десерт" to DishCategory.DESSERT,
            "!первое" to DishCategory.FIRST,
            "!второе" to DishCategory.SECOND,
            "!напиток" to DishCategory.DRINK,
            "!салат" to DishCategory.SALAD,
            "!суп" to DishCategory.SOUP,
            "!перекус" to DishCategory.SNACK,
        )
        var best: MacroHit? = null
        for ((macro, cat) in entries) {
            val idx = name.indexOf(macro, ignoreCase = true)
            if (idx >= 0 && (best == null || idx < best.start)) {
                best = MacroHit(idx, macro.length, cat)
            }
        }
        return best
    }

    private fun stripMacro(name: String, hit: MacroHit): String {
        val before = name.take(hit.start)
        val after = name.drop(hit.start + hit.length)
        return (before + after).replace(Regex("\\s+"), " ").trim()
    }

    private fun assertBjuPer100g(kbju: Kbju, portionSize: Double) {
        require(portionSize > 0) { "Размер порции должен быть больше 0" }
        val sum = kbju.protein + kbju.fat + kbju.carb
        require(sum * 100.0 / portionSize <= 100.0 + 1e-6) {
            "Сумма БЖУ на 100 г блюда не может превышать 100"
        }
    }

    private fun validateDishCommon(
        name: String,
        imagesCount: Int,
        composition: List<DishProduct>,
        portionSize: Double,
    ) {
        require(name.length >= 2) { "Название минимум 2 символа" }
        require(imagesCount <= 5) { "Не более 5 фотографий" }
        validateComposition(composition, portionSize)
    }

    private fun validateComposition(composition: List<DishProduct>, portionSize: Double) {
        require(composition.isNotEmpty()) { "Состав блюда должен содержать хотя бы один продукт" }
        require(portionSize > 0) { "Размер порции должен быть больше 0" }
        composition.forEach {
            require(it.quantity > 0) { "Количество каждого продукта в порции должно быть больше 0" }
        }
    }

    private fun DishDbo.toCompositionInputs(): List<DishProduct> =
        composition.map { DishProduct(it.product.id!!, null, it.quantity) }
}
