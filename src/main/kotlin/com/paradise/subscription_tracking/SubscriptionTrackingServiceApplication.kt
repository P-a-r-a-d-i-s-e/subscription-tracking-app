package com.paradise.subscription_tracking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SubscriptionTrackingApplication

fun main(args: Array<String>) {
    runApplication<SubscriptionTrackingApplication>(*args)
}
