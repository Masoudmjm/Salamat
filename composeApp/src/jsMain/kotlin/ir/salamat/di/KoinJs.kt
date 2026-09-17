package ir.salamat.di

import ir.salamat.core.database.DatabaseDriverFactory
import ir.salamat.core.notification.JsNotificationScheduler
import ir.salamat.core.notification.NotificationScheduler
import org.koin.dsl.module

fun initKoinJs() {
    initKoin(
        module {
            single { DatabaseDriverFactory() }
            single<NotificationScheduler> { JsNotificationScheduler() }
        }
    )
}
