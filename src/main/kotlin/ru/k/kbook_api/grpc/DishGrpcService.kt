package ru.k.kbook_api.grpc

import io.grpc.Status
import io.grpc.StatusException
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.grpc.server.service.GrpcService
import org.springframework.stereotype.Service
import ru.k.kbook_api.grpc.dish.CreateDishRequest
import ru.k.kbook_api.grpc.dish.DeleteDishRequest
import ru.k.kbook_api.grpc.dish.DeleteDishResponse
import ru.k.kbook_api.grpc.dish.Dish
import ru.k.kbook_api.grpc.dish.DishListResponse
import ru.k.kbook_api.grpc.dish.DishServiceGrpcKt
import ru.k.kbook_api.grpc.dish.GetDishRequest
import ru.k.kbook_api.grpc.dish.ListDishesRequest
import ru.k.kbook_api.grpc.dish.UpdateDishRequest
import ru.k.kbook_api.grpc.dish.ValidateDishResponse
import ru.k.kbook_api.mapper.toGrpcDish
import ru.k.kbook_api.mapper.toGrpcDishListResponse
import ru.k.kbook_api.mapper.toGrpcValidateDishResponse
import ru.k.kbook_api.mapper.toServiceCreateDishRequest
import ru.k.kbook_api.mapper.toServiceDishFilter
import ru.k.kbook_api.mapper.toServiceUpdateDishRequest
import ru.k.kbook_api.service.DishService

@Service
class DishGrpcService(
    private val dishService: DishService,
) : DishServiceGrpcKt.DishServiceCoroutineImplBase() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun createDish(request: CreateDishRequest): Dish = try {
        val dish = dishService.createDish(request.toServiceCreateDishRequest()).toGrpcDish()
        logger.info("Dish created with id ${dish.id}")
        logger.info("Successfully created dish: $dish")
        dish
    } catch (e: IllegalArgumentException) {
        logger.error(e.message)
        throw StatusException(Status.INVALID_ARGUMENT.withDescription(e.message))
    }

    override suspend fun getDish(request: GetDishRequest): Dish = try {
        val dish = dishService.getDish(request.id).toGrpcDish()
        logger.info("Successfully retrieved dish with id ${request.id}")
        dish
    } catch (e: EntityNotFoundException) {
        logger.error(e.message)
        throw StatusException(Status.NOT_FOUND.withDescription(e.message))
    }

    override suspend fun updateDish(request: UpdateDishRequest): Dish = try {
        val dish = dishService.updateDish(request.id, request.toServiceUpdateDishRequest()).toGrpcDish()
        logger.info("Successfully updated dish with id ${request.id}")
        dish
    } catch (e: IllegalArgumentException) {
        logger.error(e.message)
        throw StatusException(Status.INVALID_ARGUMENT.withDescription(e.message))
    } catch (e: EntityNotFoundException) {
        logger.error(e.message)
        throw StatusException(Status.NOT_FOUND.withDescription(e.message))
    }

    override suspend fun deleteDish(request: DeleteDishRequest): DeleteDishResponse = try {
        dishService.deleteDish(request.id)
        logger.info("Successfully deleted dish with id ${request.id}")
        DeleteDishResponse.newBuilder().setSuccess(true).build()
    } catch (e: EntityNotFoundException) {
        logger.error(e.message)
        throw StatusException(Status.NOT_FOUND.withDescription(e.message))
    }

    override suspend fun listDishes(request: ListDishesRequest): DishListResponse = try {
        val dishes = dishService.listDishes(request.toServiceDishFilter())
        val response = dishes.toGrpcDishListResponse()
        logger.info("Successfully fetched ${dishes.size} dishes")
        response
    } catch (e: Exception) {
        logger.error(e.message)
        throw StatusException(Status.INTERNAL.withDescription(e.message))
    }

    override suspend fun validateDish(request: CreateDishRequest): ValidateDishResponse = try {
        val result = dishService.validateDish(request.toServiceCreateDishRequest()).toGrpcValidateDishResponse()
        logger.info("Successfully validated dish data")
        result
    } catch (e: Exception) {
        logger.error("Validation failed: ${e.message}")
        throw StatusException(Status.INVALID_ARGUMENT.withDescription("Validation error: ${e.message}"))
    }
}
