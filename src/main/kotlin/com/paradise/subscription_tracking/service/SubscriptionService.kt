package com.paradise.subscription_tracking.service

import com.paradise.subscription_tracking.domain.Subscription
import com.paradise.subscription_tracking.domain.SubscriptionStatus
import com.paradise.subscription_tracking.dto.CreateSubscriptionDto
import com.paradise.subscription_tracking.dto.SubscriptionResponse
import com.paradise.subscription_tracking.dto.toResponse
import com.paradise.subscription_tracking.repository.SubscriptionRepository
import com.paradise.subscription_tracking.repository.SubscriptionSpecifications
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class SubscriptionService(
    private val repository: SubscriptionRepository
) {
    @Transactional
    fun create(dto: CreateSubscriptionDto): Subscription {
        return repository.save(
            Subscription(
                userId = dto.userId,
                serviceName = dto.serviceName,
                price = dto.price,
                currency = dto.currency,
                startDate = dto.startDate,
                endDate = dto.endDate
            )
        )
    }

    @Transactional
    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES)
    fun checkExpiredSubscriptions() {
        val now = OffsetDateTime.now()
        val expired = repository.findExpiredSubscriptions(now)
        expired.forEach {
            it.status = SubscriptionStatus.EXPIRED
            it.updatedAt = now
            repository.save(it)
        }
    }

    @Transactional(readOnly = true)
    fun findAll(
        userId: UUID?,
        serviceName: String?,
        status: SubscriptionStatus?,
        fromDate: OffsetDateTime?,
        toDate: OffsetDateTime?,
        pageable: Pageable
    ): Page<SubscriptionResponse> {
        val spec = SubscriptionSpecifications.withFilters(userId, serviceName, status, fromDate, toDate)
        val subscriptionPage = repository.findAll(spec, pageable)
        return subscriptionPage.map { it.toResponse() }
    }

    @Transactional
    fun cancelSubscription(id: UUID) {
        val subscription = repository.findByIdOrNull(id)
            ?: throw NoSuchElementException("Subscription with ID $id not found")
        subscription.cancel()
    }
}
