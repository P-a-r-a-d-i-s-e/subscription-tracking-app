package com.paradise.subscription_tracking.web

import com.paradise.subscription_tracking.domain.SubscriptionStatus
import com.paradise.subscription_tracking.dto.CreateSubscriptionDto
import com.paradise.subscription_tracking.dto.SubscriptionResponse
import com.paradise.subscription_tracking.dto.toResponse
import com.paradise.subscription_tracking.service.SubscriptionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscription API", description = "Управление подписками пользователей")
class SubscriptionController(private val service: SubscriptionService) {

    @PostMapping
    @Operation(summary = "Создание новой подписки")
    fun create(@Valid @RequestBody dto: CreateSubscriptionDto): ResponseEntity<SubscriptionResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(service.create(dto).toResponse())
    }

    @GetMapping
    @Operation(summary = "Получение списка подписок с фильтрацией и пагинацией")
    fun findAll(
        @RequestParam(required = false) userId: UUID?,
        @RequestParam(required = false) serviceName: String?,
        @RequestParam(required = false) status: SubscriptionStatus?,
        @RequestParam(required = false) fromDate: OffsetDateTime?,
        @RequestParam(required = false) toDate: OffsetDateTime?,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<SubscriptionResponse>> {
        return ResponseEntity.ok(service.findAll(userId, serviceName, status, fromDate, toDate, pageable))
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Отмена подписки")
    fun cancel(@PathVariable id: UUID): ResponseEntity<Unit> {
        service.cancelSubscription(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/suspend")
    @Operation(summary = "Приостановка подписки")
    fun suspend(@PathVariable id: UUID): ResponseEntity<Unit> {
        service.suspendSubscription(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/resume")
    @Operation(summary = "Возобновление подписки")
    fun resume(@PathVariable id: UUID): ResponseEntity<Unit> {
        service.resumeSubscription(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}")
    @Operation(summary = "Просмотр подписки по id")
    fun findById(@PathVariable id: UUID): ResponseEntity<SubscriptionResponse> {
        return ResponseEntity.ok(service.findSubscriptionById(id))
    }
}
