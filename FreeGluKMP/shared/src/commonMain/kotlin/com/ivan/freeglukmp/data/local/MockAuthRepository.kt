package com.ivan.freeglukmp.data.local

import com.ivan.freeglukmp.data.remote.UserResponseDTO
import com.ivan.freeglukmp.domain.model.FoodModel
import com.ivan.freeglukmp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockAuthRepository : AuthRepository {
    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    override val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()
    override val isLoggedInState: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()

    override suspend fun register(email: String, password: String): Result<UserResponseDTO> {
        return Result.success(UserResponseDTO("local-user", email))
    }

    override suspend fun login(email: String, password: String): Result<UserResponseDTO> {
        return Result.success(UserResponseDTO("local-user", email))
    }

    override fun getSavedToken(): String? = "mock-token"
    override fun logout() {
        _favoriteIds.value = emptySet()
    }
    override fun isLoggedIn(): Boolean = true
    override suspend fun syncFavorites(foodIds: List<String>): Result<Int> {
        _favoriteIds.value = foodIds.toSet()
        return Result.success(foodIds.size)
    }
    override suspend fun getRemoteFavorites(): Result<List<FoodModel>> = Result.success(emptyList())
    override suspend fun addRemoteFavorite(foodId: String): Result<Unit> {
        _favoriteIds.value = _favoriteIds.value + foodId
        return Result.success(Unit)
    }
    override suspend fun removeRemoteFavorite(foodId: String): Result<Unit> {
        _favoriteIds.value = _favoriteIds.value - foodId
        return Result.success(Unit)
    }
    override suspend fun fetchAndCacheRemoteFavorites(): Result<Unit> = Result.success(Unit)
}
