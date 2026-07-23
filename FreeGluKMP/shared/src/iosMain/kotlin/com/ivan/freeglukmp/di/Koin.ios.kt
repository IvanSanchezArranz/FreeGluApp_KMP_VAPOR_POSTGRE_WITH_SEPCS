package com.ivan.freeglukmp.di

import com.ivan.freeglukmp.data.local.DatabaseDriverFactory
import com.ivan.freeglukmp.data.local.LocalFoodRepositoryImpl
import com.ivan.freeglukmp.data.local.LocalAuthRepositoryImpl
import com.ivan.freeglukmp.data.db.AppDatabase
import com.ivan.freeglukmp.domain.repository.FoodRepository
import com.ivan.freeglukmp.domain.repository.AuthRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseDriverFactory() }
    single { AppDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single<FoodRepository> { LocalFoodRepositoryImpl(get()) }
    single<AuthRepository> { LocalAuthRepositoryImpl(get(), get()) }
}
