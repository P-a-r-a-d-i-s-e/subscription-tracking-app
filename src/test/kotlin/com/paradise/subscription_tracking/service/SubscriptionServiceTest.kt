package com.paradise.subscription_tracking.service

import com.paradise.subscription_tracking.TestcontainersConfiguration
import com.paradise.subscription_tracking.domain.SubscriptionStatus
import com.paradise.subscription_tracking.dto.CreateSubscriptionDto
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Sql("/sql/data.sql")
@Transactional
@Import(TestcontainersConfiguration::class)
@SpringBootTest
class SubscriptionServiceTest {

    @Autowired
    lateinit var subscriptionService: SubscriptionService

    @Test
    fun `should throw NoSuchElementException when subscription code does not exist`() {
        val nonExistentId = UUID.randomUUID()

        val exception = assertThrows<NoSuchElementException> {
            subscriptionService.findSubscriptionById(nonExistentId)
        }

        assertEquals("Subscription with ID $nonExistentId not found", exception.message)
    }

    @Test
    fun `should successfully create a new subscription`() {
        val dto = CreateSubscriptionDto(
            userId = UUID.randomUUID(),
            serviceName = "JetBrains IDE Premium",
            price = BigDecimal("12.00"),
            currency = "USD",
            startDate = OffsetDateTime.now(),
            endDate = OffsetDateTime.now().plusYears(1)
        )

        val created = subscriptionService.create(dto)

        assertNotNull(created.id)
        assertEquals(dto.serviceName, created.serviceName)
        assertEquals(SubscriptionStatus.ACTIVE, created.status)

        val found = subscriptionService.findSubscriptionById(created.id)
        assertEquals("JetBrains IDE Premium", found.serviceName)
    }

    @Test
    fun `should change status to EXPIRED for subscriptions whose end date has passed`() {
        // Просроченная ACTIVE подписка 'Xbox Game Pass' с end_date = '2026-05-01'
        val targetId = UUID.fromString("e5f6a7b8-c9d0-1e2f-3a4b-5c6d7e8f9a0b")

        val beforeCheck = subscriptionService.findSubscriptionById(targetId)
        assertEquals(SubscriptionStatus.ACTIVE, beforeCheck.status)

        subscriptionService.checkExpiredSubscriptions()

        val afterCheck = subscriptionService.findSubscriptionById(targetId)
        assertEquals(SubscriptionStatus.EXPIRED, afterCheck.status)
    }

    @Test
    fun `should find subscriptions page with filters`() {
        val userId = UUID.fromString("11111111-2222-3333-4444-555555555555")
        val pageable = PageRequest.of(0, 10)

        val result = subscriptionService.findAll(
            userId = userId,
            serviceName = null,
            status = null,
            fromDate = null,
            toDate = null,
            pageable = pageable
        )

        assertEquals(3, result.totalElements)
        assertEquals(3, result.content.size)
    }

    @Nested
    @DisplayName("positive outcomes of a status change")
    inner class PositiveOutcomesStatusChange {
        @Test
        fun `should cancel active subscription`() {
            val id = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")

            subscriptionService.cancelSubscription(id)

            val updated = subscriptionService.findSubscriptionById(id)
            assertEquals(SubscriptionStatus.CANCELED, updated.status)
        }

        @Test
        fun `should suspend active subscription`() {
            val id = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")

            subscriptionService.suspendSubscription(id)

            val updated = subscriptionService.findSubscriptionById(id)
            assertEquals(SubscriptionStatus.SUSPENDED, updated.status)
        }

        @Test
        fun `should resume suspended subscription`() {
            val id = UUID.fromString("b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7f")

            subscriptionService.resumeSubscription(id)

            val updated = subscriptionService.findSubscriptionById(id)
            assertEquals(SubscriptionStatus.ACTIVE, updated.status)
        }
    }

    @Nested
    @DisplayName("negative outcomes of a status change")
    inner class NegativeOutcomesStatusChange {
        @Test
        fun `cancel should throw IllegalStateException when subscription is already EXPIRED`() {
            val id = UUID.fromString("c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8e")

            val exception = assertThrows<IllegalStateException> {
                subscriptionService.cancelSubscription(id)
            }
            assertEquals("Cannot cancel expired subscription", exception.message)
        }

        @Test
        fun `suspend should throw IllegalStateException when subscription is not ACTIVE`() {
            val id = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6f")

            val exception = assertThrows<IllegalStateException> {
                subscriptionService.suspendSubscription(id)
            }
            assertEquals("Only active subscription can be suspended", exception.message)
        }

        @Test
        fun `resume should throw IllegalStateException when subscription is not SUSPENDED`() {
            val id = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")

            val exception = assertThrows<IllegalStateException> {
                subscriptionService.resumeSubscription(id)
            }
            assertEquals("Only suspended subscription can be resumed", exception.message)
        }

        @Test
        fun `resume should throw IllegalStateException when suspended subscription has expired end date`() {
            val id = UUID.fromString("a7b8c9d0-e1f2-3a4b-5c6d-7e8f9a0b1c2d")

            val exception = assertThrows<IllegalStateException> {
                subscriptionService.resumeSubscription(id)
            }
            assertEquals("Cannot activate expired subscription without renewal", exception.message)
        }
    }
}
