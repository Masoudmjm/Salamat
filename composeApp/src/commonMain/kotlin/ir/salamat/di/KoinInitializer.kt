package ir.salamat.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

fun initKoin(platformModule: Module = module { }) {
    startKoin {
        modules(appModule, platformModule)
    }
}
