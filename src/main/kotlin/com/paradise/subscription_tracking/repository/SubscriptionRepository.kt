package com.paradise.subscription_tracking.repository

import com.paradise.subscription_tracking.domain.Subscription
import com.paradise.subscription_tracking.domain.SubscriptionStatus
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.time.OffsetDateTime
import java.util.UUID

interface SubscriptionRepository : JpaRepository<Subscription, UUID>, JpaSpecificationExecutor<Subscription> {
    fun findAllByUserIdAndStatus(userId: UUID, status: SubscriptionStatus): List<Subscription>

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate < :now")
    fun findExpiredSubscriptions(now: OffsetDateTime): List<Subscription>
}

object SubscriptionSpecifications {
    fun withFilters(
        userId: UUID?,
        serviceName: String?,
        status: SubscriptionStatus?,
        fromDate: OffsetDateTime?,
        toDate: OffsetDateTime?
    ): Specification<Subscription> {
        return Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()
            userId?.let { predicates.add(cb.equal(root.get<UUID>("userId"), it)) }
            serviceName?.let { predicates.add(cb.like(cb.lower(root.get("serviceName")), "%${it.lowercase()}%")) }
            status?.let { predicates.add(cb.equal(root.get<SubscriptionStatus>("status"), it)) }
            fromDate?.let { predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), it)) }
            toDate?.let { predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), it)) }
            cb.and(*predicates.toTypedArray())
        }
    }
}