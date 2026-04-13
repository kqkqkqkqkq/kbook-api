package ru.k.kbook_api.grpc

import io.grpc.Status
import io.grpc.StatusException
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import ru.k.kbook_api.grpc.dish.CreateDishRequest
import ru.k.kbook_api.grpc.dish.DeleteDishRequest
import ru.k.kbook_api.grpc.dish.DeleteDishResponse
import ru.k.kbook_api.grpc.dish.Dish
import ru.k.kbook_api.grpc.dish.DishFilter
import ru.k.kbook_api.grpc.dish.DishListResponse
import ru.k.kbook_api.grpc.dish.DishServiceGrpcKt
import ru.k.kbook_api.grpc.dish.GetDishRequest
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

    override suspend fun createDish(request: CreateDishRequest): Dish = try {
        dishService.createDish(request.toServiceCreateDishRequest()).toGrpcDish()
    } catch (e: IllegalArgumentException) {
        throw StatusException(Status.INVALID_ARGUMENT.withDescription(e.message))
    }

    override suspend fun getDish(request: GetDishRequest): Dish = try {
        dishService.getDish(request.id).toGrpcDish()
    } catch (e: EntityNotFoundException) {
        throw StatusException(Status.NOT_FOUND.withDescription(e.message))
    }

    override suspend fun updateDish(request: UpdateDishRequest): Dish = try {
        dishService.updateDish(request.id, request.toServiceUpdateDishRequest()).toGrpcDish()
    } catch (e: IllegalArgumentException) {
        throw StatusException(Status.INVALID_ARGUMENT.withDescription(e.message))
    } catch (e: EntityNotFoundException) {
        throw StatusException(Status.NOT_FOUND.withDescription(e.message))
    }

    override suspend fun deleteDish(request: DeleteDishRequest): DeleteDishResponse = try {
        dishService.deleteDish(request.id)
        DeleteDishResponse.newBuilder().setSuccess(true).build()
    } catch (e: EntityNotFoundException) {
        throw StatusException(Status.NOT_FOUND.withDescription(e.message))
    }

    override suspend fun listDishes(request: DishFilter): DishListResponse =
        dishService.listDishes(request.toServiceDishFilter()).toGrpcDishListResponse()

    override suspend fun validateDish(request: CreateDishRequest): ValidateDishResponse =
        dishService.validateDish(request.toServiceCreateDishRequest()).toGrpcValidateDishResponse()
}
