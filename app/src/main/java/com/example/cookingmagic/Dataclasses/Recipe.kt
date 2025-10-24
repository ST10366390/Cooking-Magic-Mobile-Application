package com.example.cookingmagic.Dataclasses

data class Recipe(
    val recipeId:String,

    val name:String,
    val ingredients:List<Ingredient>,
    val favorite: Boolean
)
