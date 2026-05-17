package com.paradise.subscription_tracking

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<SubscriptionTrackingApplication>().with(TestcontainersConfiguration::class).run(*args)
}
