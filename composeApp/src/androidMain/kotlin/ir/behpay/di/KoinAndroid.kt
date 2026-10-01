package ir.behpay.di

import android.content.Context
import ir.behpay.core.database.DatabaseDriverFactory
import ir.behpay.core.notification.AndroidNotificationScheduler
import ir.behpay.core.notification.NotificationScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun initKoinAndroid(context: Context) {
    startKoin {
        androidContext(context)
        modules(
            appModule,
            module {
                single { DatabaseDriverFactory(get()) }
                single<NotificationScheduler> { AndroidNotificationScheduler(get()) }
            }
        )
    }
}
