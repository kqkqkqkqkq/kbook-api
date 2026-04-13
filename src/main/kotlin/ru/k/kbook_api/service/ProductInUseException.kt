package ru.k.kbook_api.service

class ProductInUseException(val dishNames: List<String>) : RuntimeException(
    "Невозможно удалить продукт: он используется в блюдах: ${dishNames.joinToString(", ")}",
)
