package com.paradise.subscription_tracking.domain

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "subscription")
class Subscription(
    @Id val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val serviceName: String,
    @Enumerated(EnumType.STRING) var status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val price: BigDecimal,
    val currency: String,
    val startDate: OffsetDateTime,
    var endDate: OffsetDateTime,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
) {
    fun cancel() {
        if (status == SubscriptionStatus.EXPIRED)
            throw IllegalStateException("Cannot cancel expired subscription")
        this.status = SubscriptionStatus.CANCELED
        this.updatedAt = OffsetDateTime.now()
    }

    fun suspend() {
        if (status != SubscriptionStatus.ACTIVE)
            throw IllegalStateException("Only active subscription can be suspended")
        this.status = SubscriptionStatus.SUSPENDED
        this.updatedAt = OffsetDateTime.now()
    }

    fun resume() {
        if (status != SubscriptionStatus.SUSPENDED)
            throw IllegalStateException("Only suspended subscription can be resumed")
        if (endDate.isBefore(OffsetDateTime.now()))
            throw IllegalStateException("Cannot activate expired subscription without renewal")
        this.status = SubscriptionStatus.ACTIVE
        this.updatedAt = OffsetDateTime.now()
    }
}
