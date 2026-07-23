package com.ivan.freeglukmp.data.local

import com.ivan.freeglukmp.data.db.AppDatabase
import com.ivan.freeglukmp.domain.model.FoodModel
import com.ivan.freeglukmp.domain.repository.FoodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalFoodRepositoryImpl(
    private val database: AppDatabase
) : FoodRepository {

    private val queries = database.appDatabaseQueries

    override suspend fun getFoods(page: Int, per: Int): Result<List<FoodModel>> = withContext(Dispatchers.Default) {
        try {
            val limit = per.toLong()
            val offset = ((page - 1) * per).toLong()
            val foods = queries.getFoods(limit = limit, offset = offset).executeAsList().map { entity ->
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

    override suspend fun searchFoods(query: String, page: Int, per: Int): Result<List<FoodModel>> = withContext(Dispatchers.Default) {
        try {
            val limit = per.toLong()
            val offset = ((page - 1) * per).toLong()
            val likeQuery = "%$query%"
            val foods = queries.searchFoods(query = likeQuery, limit = limit, offset = offset).executeAsList().map { entity ->
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

    override suspend fun getFoodDetail(id: String): Result<FoodModel> = withContext(Dispatchers.Default) {
        try {
            val entity = queries.getFoodById(id).executeAsOneOrNull()
            if (entity != null) {
                val override = queries.getFoodOverride(userId = "local-user", foodId = id).executeAsOneOrNull()
                val finalIngredients = if (override?.customNotes != null && override.customNotes.isNotBlank()) {
                    entity.ingredients + "\n\n[Nota Local]: " + override.customNotes
                } else {
                    entity.ingredients
                }
                
                val model = FoodModel(
                    id = entity.id,
                    code = entity.code,
                    name = entity.name,
                    brand = entity.brand,
                    categories = entity.categories.split(",").filter { it.isNotBlank() },
                    ingredients = finalIngredients,
                    imageUrl = entity.imageUrl,
                    isGlutenFree = entity.isGlutenFree
                )
                Result.success(model)
            } else {
                Result.failure(Exception("Alimento no encontrado localmente"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createFood(food: FoodModel): Result<FoodModel> = withContext(Dispatchers.Default) {
        try {
            queries.insertFood(
                id = food.id,
                code = food.code,
                name = food.name,
                brand = food.brand,
                categories = food.categories.joinToString(","),
                ingredients = food.ingredients,
                imageUrl = food.imageUrl,
                isGlutenFree = food.isGlutenFree
            )
            Result.success(food)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFood(id: String, food: FoodModel): Result<FoodModel> = withContext(Dispatchers.Default) {
        try {
            queries.insertFood(
                id = id,
                code = food.code,
                name = food.name,
                brand = food.brand,
                categories = food.categories.joinToString(","),
                ingredients = food.ingredients,
                imageUrl = food.imageUrl,
                isGlutenFree = food.isGlutenFree
            )
            
            // Si el nombre contiene alguna nota local extra, la podemos guardar en overrides
            if (food.ingredients.contains("[Nota Local]:")) {
                val parts = food.ingredients.split("[Nota Local]:")
                if (parts.size > 1) {
                    queries.insertFoodOverride(userId = "local-user", foodId = id, customNotes = parts[1].trim())
                }
            }

            Result.success(food)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFood(id: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            queries.deleteFood(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
