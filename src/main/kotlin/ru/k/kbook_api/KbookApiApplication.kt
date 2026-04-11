package ru.k.kbook_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KbookApiApplication

fun main(args: Array<String>) {
	runApplication<KbookApiApplication>(*args)
}
