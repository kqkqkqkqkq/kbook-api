package ru.k.kbook_api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.k.kbook_api.repository.entity.Product

@Repository
interface ProductRepository : JpaRepository<Product, Long>
