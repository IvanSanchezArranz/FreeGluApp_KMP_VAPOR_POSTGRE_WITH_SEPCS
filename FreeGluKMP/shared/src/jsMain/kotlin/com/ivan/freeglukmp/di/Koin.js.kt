package com.ivan.freeglukmp.di

import com.ivan.freeglukmp.data.local.MockFoodRepository
import com.ivan.freeglukmp.data.local.MockAuthRepository
import com.ivan.freeglukmp.domain.repository.FoodRepository
import com.ivan.freeglukmp.domain.repository.AuthRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<FoodRepository> { MockFoodRepository() }
    single<AuthRepository> { MockAuthRepository() }
}
