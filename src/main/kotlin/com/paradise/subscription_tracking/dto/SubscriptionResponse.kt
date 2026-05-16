package com.paradise.subscription_tracking.dto

import com.paradise.subscription_tracking.domain.Subscription
import com.paradise.subscription_tracking.domain.SubscriptionStatus
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

data class SubscriptionResponse(
    val id: UUID,
    val userId: UUID,
    val serviceName: String,
    val status: SubscriptionStatus,
    val price: BigDecimal,
    val currency: String,
    val startDate: OffsetDateTime,
    val endDate: OffsetDateTime
)

fun Subscription.toResponse(): SubscriptionResponse {
    return SubscriptionResponse(
        id = this.id,
        userId = this.userId,
        serviceName = this.serviceName,
        status = this.status,
        price = this.price,
        currency = this.currency,
        startDate = this.startDate,
        endDate = this.endDate
    )
}
