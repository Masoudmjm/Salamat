package ir.behpay.di

import ir.behpay.core.database.DatabaseProvider
import ir.behpay.core.notification.ReminderSyncEngine
import ir.behpay.data.repository.CheckupRepository
import ir.behpay.data.repository.CheckupRepositoryImpl
import ir.behpay.data.repository.GrowthRepository
import ir.behpay.data.repository.GrowthRepositoryImpl
import ir.behpay.data.repository.ProfileRepository
import ir.behpay.data.repository.ProfileRepositoryImpl
import ir.behpay.data.repository.VaccineRepository
import ir.behpay.data.repository.VaccineRepositoryImpl
import ir.behpay.ui.AppViewModel
import ir.behpay.ui.screens.checkup.CheckupViewModel
import ir.behpay.ui.screens.growth.GrowthViewModel
import ir.behpay.ui.screens.vaccine.VaccineViewModel
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
