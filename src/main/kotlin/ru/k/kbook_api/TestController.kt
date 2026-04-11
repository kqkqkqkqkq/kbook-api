package ru.k.kbook_api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.k.kbook_api.repository.TestRepository
import ru.k.kbook_api.repository.entity.Test

@RestController
@RequestMapping("/test")
class TestController(
    private val repository: TestRepository
) {

    @GetMapping
    fun test(): List<Test> {
        return repository.findAll()
    }
}
