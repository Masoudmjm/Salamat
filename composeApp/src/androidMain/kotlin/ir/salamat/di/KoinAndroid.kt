package ir.salamat.di

import android.content.Context
import ir.salamat.core.database.DatabaseDriverFactory
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
            }
        )
    }
}
