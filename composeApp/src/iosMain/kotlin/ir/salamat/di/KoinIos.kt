package ir.salamat.di

import ir.salamat.core.database.DatabaseDriverFactory
import ir.salamat.core.notification.IosNotificationScheduler
import ir.salamat.core.notification.NotificationScheduler
import org.koin.dsl.module

fun initKoinIos() {
    initKoin(
        module {
            single { DatabaseDriverFactory() }
            single<NotificationScheduler> { IosNotificationScheduler() }
        }
    )
}
