package ir.salamat.di

import ir.salamat.core.database.DatabaseDriverFactory
import ir.salamat.core.database.DatabaseProvider
import ir.salamat.core.notification.ReminderSyncEngine
import ir.salamat.data.repository.CheckupRepository
import ir.salamat.data.repository.CheckupRepositoryImpl
import ir.salamat.data.repository.GrowthRepository
import ir.salamat.data.repository.GrowthRepositoryImpl
import ir.salamat.data.repository.ProfileRepository
import ir.salamat.data.repository.ProfileRepositoryImpl
import ir.salamat.data.repository.VaccineRepository
import ir.salamat.data.repository.VaccineRepositoryImpl
import ir.salamat.ui.AppViewModel
import ir.salamat.ui.screens.checkup.CheckupViewModel
import ir.salamat.ui.screens.growth.GrowthViewModel
import ir.salamat.ui.screens.vaccine.VaccineViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {
    single { DatabaseProvider(get()) }

    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    single<VaccineRepository> { VaccineRepositoryImpl(get()) }
    single<GrowthRepository> { GrowthRepositoryImpl(get()) }
    single<CheckupRepository> { CheckupRepositoryImpl(get()) }
    single { ReminderSyncEngine(get()) }

    factory { AppViewModel(get(), get(), get(), get(), get()) }
    factory { (profileId: String) -> VaccineViewModel(profileId, get(), get()) }
    factory { (profileId: String) -> GrowthViewModel(profileId, get(), get()) }
    factory { (profileId: String) -> CheckupViewModel(profileId, get(), get()) }
}
