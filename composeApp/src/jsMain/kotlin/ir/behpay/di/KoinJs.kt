package ir.behpay.di

import ir.behpay.core.database.DatabaseDriverFactory
import ir.behpay.core.notification.JsNotificationScheduler
import ir.behpay.core.notification.NotificationScheduler
import org.koin.dsl.module

fun initKoinJs() {
    initKoin(
        module {
            single { DatabaseDriverFactory() }
            single<NotificationScheduler> { JsNotificationScheduler() }
        }
    )
}
