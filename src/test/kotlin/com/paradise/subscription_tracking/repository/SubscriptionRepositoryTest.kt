package com.paradise.subscription_tracking.repository

import com.paradise.subscription_tracking.TestcontainersConfiguration
import com.paradise.subscription_tracking.domain.SubscriptionStatus
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Sql("classpath:sql/data.sql")
@Transactional
@Import(TestcontainersConfiguration::class)
@SpringBootTest
class SubscriptionRepositoryTest {

    @Autowired
    lateinit var subscriptionRepository: SubscriptionRepository

    @Test
    fun `should find active subscriptions for user 1`() {
        // Пользователь 1: '11111111-2222-3333-4444-555555555555'
        // Имеет 3 подписки: 2 ACTIVE (Netflix, Spotify) и 1 CANCELED (YouTube)
        val userId = UUID.fromString("11111111-2222-3333-4444-555555555555")

        val activeSubscriptions = subscriptionRepository.findAllByUserIdAndStatus(
            userId,
            SubscriptionStatus.ACTIVE
        )

        Assertions.assertEquals(2, activeSubscriptions.size)
        val names = activeSubscriptions.map { it.serviceName }
        Assertions.assertTrue(names.contains("Netflix Premium"))
        Assertions.assertTrue(names.contains("Spotify Premium"))
    }

    @Test
    fun `should return empty list when user has no subscriptions with such status`() {
        val userId = UUID.fromString("11111111-2222-3333-4444-555555555555")

        // У Пользователя 1 нет подписок со статусом SUSPENDED
        val suspendedSubscriptions =
            subscriptionRepository.findAllByUserIdAndStatus(userId, SubscriptionStatus.SUSPENDED)

        Assertions.assertTrue(suspendedSubscriptions.isEmpty())
    }

    @Test
    fun `should find active subscriptions that have expired relative to the given date`() {
        // У Пользователя 1 подписка Netflix ACTIVE имеет end_date = '2026-06-01'
        val customNow = OffsetDateTime.of(
            2026, 7, 1, 0, 0,
            0, 0, ZoneOffset.UTC
        )

        val expiredActiveSubscriptions = subscriptionRepository.findExpiredSubscriptions(customNow)

        // Метод должен найти как минимум Netflix Premium (end_date: 2026-06-01)
        // и другие ACTIVE подписки, у которых end_date наступил раньше июля 2026
        Assertions.assertTrue(expiredActiveSubscriptions.isNotEmpty())
        val hasNetflix = expiredActiveSubscriptions.any { it.serviceName == "Netflix Premium" }
        Assertions.assertTrue(hasNetflix, "Должна вернуться просроченная подписка Netflix")
    }

    @Test
    fun `should filter by partial service name case-insensitive`() {
        val spec = SubscriptionSpecifications.withFilters(
            userId = null,
            serviceName = "pReMiUm",
            status = null,
            fromDate = null,
            toDate = null
        )

        val result = subscriptionRepository.findAll(spec)

        Assertions.assertTrue(result.size >= 5)
        Assertions.assertTrue(result.all { it.serviceName.contains("Premium", ignoreCase = true) })
    }

    @Test
    fun `should combine multiple filters correctly`() {
        val userId = UUID.fromString("11111111-2222-3333-4444-555555555555")

        val spec = SubscriptionSpecifications.withFilters(
            userId = userId,
            serviceName = null,
            status = SubscriptionStatus.ACTIVE,
            fromDate = null,
            toDate = null
        )

        val result = subscriptionRepository.findAll(spec)

        Assertions.assertEquals(2, result.size)
        Assertions.assertTrue(result.all { it.userId == userId && it.status == SubscriptionStatus.ACTIVE })
    }

    @Test
    fun `should filter by end date range`() {
        // В данных есть подписка Miro Starter (CANCELED) с end_date = '2025-11-01'
        val fromDate = OffsetDateTime.of(
            2025, 10, 1, 0,
            0, 0, 0, ZoneOffset.UTC
        )
        val toDate = OffsetDateTime.of(
            2025, 11, 15, 0,
            0, 0, 0, ZoneOffset.UTC
        )

        val spec = SubscriptionSpecifications.withFilters(
            userId = null,
            serviceName = null,
            status = null,
            fromDate = fromDate,
            toDate = toDate
        )

        val result = subscriptionRepository.findAll(spec)

        Assertions.assertTrue(result.isNotEmpty())
        Assertions.assertTrue(result.all { it.endDate.isAfter(fromDate) && it.endDate.isBefore(toDate) })
    }
}
