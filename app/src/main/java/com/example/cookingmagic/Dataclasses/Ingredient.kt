package com.example.cookingmagic.Dataclasses

data class Ingredient(
    val ingredientId: String,
    val recipeId:String,
    val name:String,
    val quantity: Double,
    val unitMeasure:String,
)
