package com.paradise.subscription_tracking.dto

import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

data class CreateSubscriptionDto(
    @field:NotNull(message = "User ID is required")
    var userId: UUID,

    @field:NotBlank(message = "Service name cannot be empty")
    @field:Size(max = 255, message = "Service name must be under 255 characters")
    val serviceName: String,

    @field:NotNull(message = "Price is required")
    @field:Positive(message = "Price must be greater than zero")
    var price: BigDecimal,

    @field:NotBlank(message = "Currency code cannot be empty")
    @field:Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    val currency: String,

    @field:NotNull(message = "Start date is required")
    var startDate: OffsetDateTime,

    @field:NotNull(message = "End date is required")
    @field:FutureOrPresent(message = "End date must be in the present or future")
    var endDate: OffsetDateTime
) {
    init {
        if (endDate.isBefore(startDate)) {
            throw IllegalArgumentException("End date must be strictly after start date")
        }
    }
}
