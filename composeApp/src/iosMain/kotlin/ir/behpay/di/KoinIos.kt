package ir.behpay.di

import ir.behpay.core.database.DatabaseDriverFactory
import ir.behpay.core.notification.IosNotificationScheduler
import ir.behpay.core.notification.NotificationScheduler
import org.koin.dsl.module

fun initKoinIos() {
    initKoin(
        module {
            single { DatabaseDriverFactory() }
            single<NotificationScheduler> { IosNotificationScheduler() }
        }
    )
}
