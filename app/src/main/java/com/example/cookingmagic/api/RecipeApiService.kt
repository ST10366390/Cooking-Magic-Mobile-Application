package com.example.cookingmagic.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// Data class for a recipe
data class Recipe(
    val title: String,
    val ingredients: String,
    val servings: String,
    val instructions: String
)

// Interface for the Recipe API service
interface RecipeApiService {
    @GET("recipe")
    fun searchRecipes(
        @Query("query") query: String,
        @Header("X-Api-Key") apiKey: String = RetrofitClient.API_KEY
    ): Call<List<Recipe>>
}