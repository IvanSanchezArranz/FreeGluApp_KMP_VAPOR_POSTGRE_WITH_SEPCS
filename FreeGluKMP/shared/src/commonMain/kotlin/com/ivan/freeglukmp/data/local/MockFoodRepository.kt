package com.ivan.freeglukmp.data.local

import com.ivan.freeglukmp.domain.model.FoodModel
import com.ivan.freeglukmp.domain.repository.FoodRepository

class MockFoodRepository : FoodRepository {
    private val foods = mutableListOf<FoodModel>()

    override suspend fun getFoods(page: Int, per: Int): Result<List<FoodModel>> {
        return Result.success(foods)
    }

    override suspend fun searchFoods(query: String, page: Int, per: Int): Result<List<FoodModel>> {
        return Result.success(foods.filter { it.name.contains(query, ignoreCase = true) })
    }

    override suspend fun getFoodDetail(id: String): Result<FoodModel> {
        val food = foods.find { it.id == id }
        return if (food != null) Result.success(food) else Result.failure(Exception("Not found"))
    }

    override suspend fun createFood(food: FoodModel): Result<FoodModel> {
        foods.add(food)
        return Result.success(food)
    }

    override suspend fun updateFood(id: String, food: FoodModel): Result<FoodModel> {
        foods.removeAll { it.id == id }
        foods.add(food)
        return Result.success(food)
    }

    override suspend fun deleteFood(id: String): Result<Unit> {
        foods.removeAll { it.id == id }
        return Result.success(Unit)
    }
}
