package ru.k.kbook_api.service

import org.springframework.grpc.server.service.GrpcService
import ru.k.kbook_api.grpc.HelloReply
import ru.k.kbook_api.grpc.HelloRequest
import ru.k.kbook_api.grpc.TestServiceGrpcKt
import ru.k.kbook_api.repository.TestRepository
import ru.k.kbook_api.repository.entity.Test

@GrpcService
class TestServiceImpl(
    private val repository: TestRepository,
): TestServiceGrpcKt.TestServiceCoroutineImplBase()  {

    override suspend fun hello(request: HelloRequest): HelloReply {
        repository.save(Test(name = request.name))
        return HelloReply.newBuilder()
            .setMessage("Hello, ${request.name}")
            .build()
    }
}
