package com.ivan.freeglukmp.data.local

import com.ivan.freeglukmp.data.db.AppDatabase
import com.ivan.freeglukmp.data.remote.UserResponseDTO
import com.ivan.freeglukmp.domain.model.FoodModel
import com.ivan.freeglukmp.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocalAuthRepositoryImpl(
    private val database: AppDatabase,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    private val queries = database.appDatabaseQueries
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    override val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _isLoggedInState = MutableStateFlow(tokenStorage.getToken() != null)
    override val isLoggedInState: StateFlow<Boolean> = _isLoggedInState.asStateFlow()

    init {
        // En un entorno Offline, si no hay token guardado, creamos una sesión por defecto
        if (tokenStorage.getToken() == null) {
            tokenStorage.saveToken("local-user-token")
            _isLoggedInState.value = true
        }
        
        // Cargar favoritos al arrancar
        coroutineScope.launch {
            loadLocalFavorites()
        }
    }

    private suspend fun loadLocalFavorites() = withContext(Dispatchers.Default) {
        try {
            val ids = queries.getFavoriteIdsForUser("local-user").executeAsList().toSet()
            _favoriteIds.value = ids
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun register(email: String, password: String): Result<UserResponseDTO> = withContext(Dispatchers.Default) {
        try {
            queries.insertUser(id = "local-user", email = email, passwordHash = password)
            tokenStorage.saveToken("local-user-token")
            _isLoggedInState.value = true
            Result.success(UserResponseDTO(id = "local-user", email = email))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<UserResponseDTO> = withContext(Dispatchers.Default) {
        try {
            val user = queries.getUserByEmail(email).executeAsOneOrNull()
            if (user != null && user.passwordHash == password) {
                tokenStorage.saveToken("local-user-token")
                _isLoggedInState.value = true
                loadLocalFavorites()
                Result.success(UserResponseDTO(id = user.id, email = user.email))
            } else {
                // Para simplificar la primera ejecución offline, si no existe el usuario, lo auto-registramos
                queries.insertUser(id = "local-user", email = email, passwordHash = password)
                tokenStorage.saveToken("local-user-token")
                _isLoggedInState.value = true
                loadLocalFavorites()
                Result.success(UserResponseDTO(id = "local-user", email = email))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSavedToken(): String? {
        return tokenStorage.getToken()
    }

    override fun logout() {
        tokenStorage.clearToken()
        _favoriteIds.value = emptySet()
        _isLoggedInState.value = false
    }

    override fun isLoggedIn(): Boolean {
        return tokenStorage.getToken() != null
    }

    override suspend fun syncFavorites(foodIds: List<String>): Result<Int> = withContext(Dispatchers.Default) {
        try {
            foodIds.forEach { foodId ->
                queries.insertFavorite(userId = "local-user", foodId = foodId)
            }
            loadLocalFavorites()
            Result.success(foodIds.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRemoteFavorites(): Result<List<FoodModel>> = withContext(Dispatchers.Default) {
        try {
            val foods = queries.getFavoritesForUser("local-user").executeAsList().map { entity ->
                FoodModel(
                    id = entity.id,
                    code = entity.code,
                    name = entity.name,
                    brand = entity.brand,
                    categories = entity.categories.split(",").filter { it.isNotBlank() },
                    ingredients = entity.ingredients,
                    imageUrl = entity.imageUrl,
                    isGlutenFree = entity.isGlutenFree
                )
            }
            Result.success(foods)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchAndCacheRemoteFavorites(): Result<Unit> {
        loadLocalFavorites()
        return Result.success(Unit)
    }

    override suspend fun addRemoteFavorite(foodId: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            queries.insertFavorite(userId = "local-user", foodId = foodId)
            loadLocalFavorites()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeRemoteFavorite(foodId: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            queries.deleteFavorite(userId = "local-user", foodId = foodId)
            loadLocalFavorites()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
