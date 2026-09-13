package ir.salamat.di

import ir.salamat.core.database.DatabaseDriverFactory
import org.koin.dsl.module

fun initKoinIos() {
    initKoin(
        module {
            single { DatabaseDriverFactory() }
        }
    )
}
