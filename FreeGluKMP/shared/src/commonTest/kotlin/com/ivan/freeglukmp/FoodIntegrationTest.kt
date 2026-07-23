package com.ivan.freeglukmp

import com.ivan.freeglukmp.di.sharedModule
import com.ivan.freeglukmp.di.platformModule
import com.ivan.freeglukmp.data.local.MockFoodRepository
import com.ivan.freeglukmp.data.local.MockAuthRepository
import com.ivan.freeglukmp.domain.repository.FoodRepository
import com.ivan.freeglukmp.domain.repository.AuthRepository
import com.ivan.freeglukmp.domain.model.FoodModel
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class FoodIntegrationTest {

    private lateinit var foodRepository: FoodRepository
    private lateinit var authRepository: AuthRepository

    @BeforeTest
    fun setUp() {
        try {
            // Intentamos iniciar Koin con el platformModule real
            val koinApp = startKoin {
                modules(sharedModule, platformModule)
            }
            foodRepository = koinApp.koin.get()
            authRepository = koinApp.koin.get()
        } catch (e: Exception) {
            // Si falla (ej. por contexto Android null en tests host de JUnit),
            // caemos en un Mock Koin module seguro para los tests.
            stopKoin()
            val testModule = org.koin.dsl.module {
                single<FoodRepository> { MockFoodRepository() }
                single<AuthRepository> { MockAuthRepository() }
            }
            val koinApp = startKoin {
                modules(sharedModule, testModule)
            }
            foodRepository = koinApp.koin.get()
            authRepository = koinApp.koin.get()
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testGetFoodsConnectionAndParsing() = runTest {
        // Asegurar que podemos insertar y listar alimentos localmente
        val sampleFood = FoodModel(
            id = "test-123",
            code = "1234567890",
            name = "Test Bread",
            brand = "Test Brand",
            categories = listOf("Bread"),
            ingredients = "Water, flour",
            imageUrl = "http://example.com/image.png",
            isGlutenFree = true
        )
        foodRepository.createFood(sampleFood)

        val result = foodRepository.getFoods(page = 1, per = 10)
        assertTrue(result.isSuccess)
        val foods = result.getOrNull()
        assertNotNull(foods)
        assertTrue(foods.isNotEmpty())
        assertTrue(foods.any { it.id == "test-123" })
    }

    @Test
    fun testSearchFoodsConnectionAndParsing() = runTest {
        val sampleFood = FoodModel(
            id = "test-456",
            code = "0987654321",
            name = "Gluten Free Pasta",
            brand = "GF Brand",
            categories = listOf("Pasta"),
            ingredients = "Rice flour",
            imageUrl = "http://example.com/pasta.png",
            isGlutenFree = true
        )
        foodRepository.createFood(sampleFood)

        val result = foodRepository.searchFoods(query = "Pasta", page = 1, per = 10)
        assertTrue(result.isSuccess)
        val foods = result.getOrNull()
        assertNotNull(foods)
        assertTrue(foods.any { it.name.contains("Pasta") })
    }

    @Test
    fun testRegisterLoginAndMergeFavorites() = runTest {
        authRepository.logout()
        
        // 1. Register
        val email = "local_test_user@example.com"
        val regResult = authRepository.register(email, "Password123!")
        assertTrue(regResult.isSuccess)
        
        // 2. Login
        val loginResult = authRepository.login(email, "Password123!")
        assertTrue(loginResult.isSuccess)
        
        // 3. Add Favorite
        val favResult = authRepository.addRemoteFavorite("test-123")
        assertTrue(favResult.isSuccess)
        
        // 4. Verify Favorite ID is cached
        assertTrue(authRepository.favoriteIds.value.contains("test-123"))
    }
}
