package ir.salamat.di

import ir.salamat.core.database.DatabaseDriverFactory
import org.koin.dsl.module

fun initKoinJs() {
    initKoin(
        module {
            single { DatabaseDriverFactory() }
        }
    )
}
