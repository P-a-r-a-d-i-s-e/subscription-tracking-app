package com.paradise.subscription_tracking.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.paradise.subscription_tracking.domain.Subscription
import com.paradise.subscription_tracking.domain.SubscriptionStatus
import com.paradise.subscription_tracking.dto.CreateSubscriptionDto
import com.paradise.subscription_tracking.dto.SubscriptionResponse
import com.paradise.subscription_tracking.service.SubscriptionService
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@WebMvcTest(SubscriptionController::class)
class SubscriptionControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var subscriptionService: SubscriptionService

    private fun createMockResponse(id: UUID = UUID.randomUUID()): SubscriptionResponse {
        return SubscriptionResponse(
            id = id,
            userId = UUID.randomUUID(),
            serviceName = "Netflix",
            status = SubscriptionStatus.ACTIVE,
            price = BigDecimal("19.99"),
            currency = "USD",
            startDate = OffsetDateTime.now(),
            endDate = OffsetDateTime.now().plusMonths(1)
        )
    }

    @Nested
    inner class SuccessScenarios {

        @Test
        fun `should create subscription and return 201 Created`() {
            val dto = CreateSubscriptionDto(
                userId = UUID.randomUUID(),
                serviceName = "Spotify",
                price = BigDecimal("9.99"),
                currency = "USD",
                startDate = OffsetDateTime.now(),
                endDate = OffsetDateTime.now().plusMonths(1)
            )
            val mockSubscription = Subscription(
                id = UUID.randomUUID(),
                userId = dto.userId,
                serviceName = dto.serviceName,
                price = dto.price,
                currency = dto.currency,
                startDate = dto.startDate,
                endDate = dto.endDate
            )

            `when`(subscriptionService.create(any())).thenReturn(mockSubscription)

            mockMvc.perform(
                post("/api/v1/subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.serviceName").value("Spotify"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
        }

        @Test
        fun `should get subscription by id and return 200 OK`() {
            val id = UUID.randomUUID()
            val response = createMockResponse(id)

            `when`(subscriptionService.findSubscriptionById(id)).thenReturn(response)

            mockMvc.perform(get("/api/v1/subscriptions/$id"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(id.toString()))
        }

        @Test
        fun `should find all subscriptions with filters and return 200 OK`() {
            val responsePage = PageImpl(listOf(createMockResponse()))

            `when`(
                subscriptionService.findAll(
                    anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(),
                    anyOrNull(), any()
                )
            ).thenReturn(responsePage)

            mockMvc.perform(
                get("/api/v1/subscriptions")
                    .param("serviceName", "Netflix")
                    .param("status", "ACTIVE")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content[0].serviceName").value("Netflix"))
        }

        @Test
        fun `should cancel subscription and return 204 No Content`() {
            val id = UUID.randomUUID()
            doNothing().`when`(subscriptionService).cancelSubscription(id)

            mockMvc.perform(patch("/api/v1/subscriptions/$id/cancel"))
                .andExpect(status().isNoContent)

            verify(subscriptionService).cancelSubscription(id)
        }

        @Test
        fun `should suspend subscription and return 204 No Content`() {
            val id = UUID.randomUUID()
            doNothing().`when`(subscriptionService).suspendSubscription(id)

            mockMvc.perform(patch("/api/v1/subscriptions/$id/suspend"))
                .andExpect(status().isNoContent)
        }

        @Test
        fun `should resume subscription and return 204 No Content`() {
            val id = UUID.randomUUID()
            doNothing().`when`(subscriptionService).resumeSubscription(id)

            mockMvc.perform(patch("/api/v1/subscriptions/$id/resume"))
                .andExpect(status().isNoContent)
        }
    }

    @Nested
    inner class ErrorScenarios {

        @Test
        fun `should return 404 Not Found when subscription does not exist`() {
            val id = UUID.randomUUID()
            `when`(subscriptionService.findSubscriptionById(id))
                .thenThrow(NoSuchElementException("Subscription with ID $id not found"))

            mockMvc.perform(get("/api/v1/subscriptions/$id"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should return 400 Bad Request when validation fails on create`() {
            mockMvc.perform(
                post("/api/v1/subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 400 Bad Request when transition state is invalid`() {
            val id = UUID.randomUUID()

            doThrow(IllegalStateException("Cannot cancel expired subscription"))
                .`when`(subscriptionService).cancelSubscription(id)

            mockMvc.perform(patch("/api/v1/subscriptions/$id/cancel"))
                .andExpect(status().isBadRequest)
        }
    }
}
