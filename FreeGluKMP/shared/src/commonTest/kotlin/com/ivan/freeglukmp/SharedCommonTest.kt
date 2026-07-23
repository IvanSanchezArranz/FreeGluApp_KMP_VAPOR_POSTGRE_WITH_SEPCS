package com.ivan.freeglukmp

import com.ivan.freeglukmp.di.sharedModule
import com.ivan.freeglukmp.data.local.MockFoodRepository
import com.ivan.freeglukmp.data.local.MockAuthRepository
import com.ivan.freeglukmp.domain.repository.FoodRepository
import com.ivan.freeglukmp.domain.repository.AuthRepository
import com.ivan.freeglukmp.presentation.list.FoodsListViewModel
import com.ivan.freeglukmp.presentation.detail.AddEditFoodViewModel
import com.ivan.freeglukmp.presentation.detail.AddEditFoodState
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SharedCommonTest {

    private val testModule = module {
        single<FoodRepository> { MockFoodRepository() }
        single<AuthRepository> { MockAuthRepository() }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testFoodsListViewModelIsRegisteredAsSingle() {
        // Create a local Koin application with our sharedModule and testModule
        val koinApp = koinApplication {
            modules(sharedModule, testModule)
        }
        
        val koin = koinApp.koin
        
        // Resolve the FoodsListViewModel twice
        val vm1 = koin.get<FoodsListViewModel>()
        val vm2 = koin.get<FoodsListViewModel>()
        
        // Assert that they are the exact same instance (proving 'single' registration)
        assertSame(vm1, vm2, "FoodsListViewModel must be a singleton in Koin")
    }

    @Test
    fun testFoodsListViewModelPreservesSelectedCategoryAndScrollState() {
        val koinApp = koinApplication {
            modules(sharedModule, testModule)
        }
        val koin = koinApp.koin
        val vm = koin.get<FoodsListViewModel>()
        
        // Verify default category is "All"
        assertEquals("All", vm.selectedCategory.value)
        
        // Change selected category
        vm.onCategorySelected("Bread")
        assertEquals("Bread", vm.selectedCategory.value)
        
        // Check gridState is initialized and exists
        val gridState = vm.gridState
        assertEquals(0, gridState.firstVisibleItemIndex)
        assertEquals(0, gridState.firstVisibleItemScrollOffset)
    }

    @Test
    fun testAddEditFoodViewModelStrictValidations() {
        val koinApp = koinApplication {
            modules(sharedModule, testModule)
        }
        val koin = koinApp.koin
        val vm = koin.get<AddEditFoodViewModel>()

        // 1. Empty barcode test
        vm.code.value = ""
        vm.saveProduct()
        val state1 = vm.state.value
        assertTrue(state1 is AddEditFoodState.Error)
        assertEquals("Barcode / Product Code is required and cannot be empty.", state1.message)

        // 2. Non-numeric barcode test
        vm.code.value = "1234abcd"
        vm.saveProduct()
        val state2 = vm.state.value
        assertTrue(state2 is AddEditFoodState.Error)
        assertEquals("Invalid Barcode format. It must contain only numeric digits (0-9). Spaces, letters, or symbols are not allowed.", state2.message)

        // 3. Short barcode test
        vm.code.value = "12345"
        vm.saveProduct()
        val state3 = vm.state.value
        assertTrue(state3 is AddEditFoodState.Error)
        assertTrue(state3.message.contains("Invalid Barcode length"))

        // 4. Short product name test
        vm.code.value = "12345678"
        vm.name.value = "Ab"
        vm.saveProduct()
        val state4 = vm.state.value
        assertTrue(state4 is AddEditFoodState.Error)
        assertEquals("Product Name is too short. It must be at least 3 characters long to ensure identification.", state4.message)

        // 5. Invalid Image URL test
        vm.name.value = "Valid Name"
        vm.brand.value = "Valid Brand"
        vm.categories.value = "Valid Category"
        vm.imageUrl.value = "invalid_url_no_protocol"
        vm.saveProduct()
        val state5 = vm.state.value
        assertTrue(state5 is AddEditFoodState.Error)
        assertEquals("Invalid Image URL format. It must start with 'http://' or 'https://' protocol prefixes.", state5.message)
    }
}
