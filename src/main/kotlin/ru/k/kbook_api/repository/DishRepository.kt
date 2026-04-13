package ru.k.kbook_api.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.k.kbook_api.repository.entity.dish.DishDbo

interface DishRepository : JpaRepository<DishDbo, Long> {

    @EntityGraph(attributePaths = ["images", "composition", "composition.product", "composition.product.flags"])
    @Query("SELECT DISTINCT d FROM DishDbo d")
    fun findAllWithDetails(): List<DishDbo>

    @Query("SELECT DISTINCT d.name FROM DishDbo d JOIN d.composition c WHERE c.product.id = :productId")
    fun findDishNamesUsingProduct(@Param("productId") productId: Long): List<String>
}
