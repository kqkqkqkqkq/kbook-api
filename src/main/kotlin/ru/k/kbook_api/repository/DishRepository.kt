package ru.k.kbook_api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.k.kbook_api.repository.entity.dish.DishDbo

@Repository
interface DishRepository : JpaRepository<DishDbo, Long> {

    @Query("""
        SELECT DISTINCT d FROM DishDbo d
        LEFT JOIN FETCH d.images
        LEFT JOIN FETCH d.products dp
        LEFT JOIN FETCH dp.product p
        LEFT JOIN FETCH p.flags
    """)
    fun findAllWithDetails(): List<DishDbo>

    @Query("""
        SELECT DISTINCT d.name 
        FROM DishDbo d 
        JOIN d.products dp 
        WHERE dp.product.id = :productId
    """)
    fun findDishNamesUsingProduct(@Param("productId") productId: Long): List<String>
}
