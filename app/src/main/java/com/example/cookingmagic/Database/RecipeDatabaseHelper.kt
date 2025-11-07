package com.example.cookingmagic.Dataclasses

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// Ingredient data class as provided


// Database Helper for SQLite
class RecipeDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "recipes.db"
        private const val DATABASE_VERSION = 1

        // Recipes table
        private const val TABLE_RECIPES = "recipes"
        private const val COLUMN_RECIPE_ID = "recipeId"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_FAVORITE = "favorite"

        // Ingredients table
        private const val TABLE_INGREDIENTS = "ingredients"
        private const val COLUMN_INGREDIENT_ID = "ingredientId"
        private const val COLUMN_RECIPE_ID_FK = "recipeId"

        private const val COLUMN_QUANTITY = "quantity"
        private const val COLUMN_UNIT_MEASURE = "unitMeasure"

        // Local favorites table for offline storage
        private const val TABLE_LOCAL_FAVORITES = "local_favorites"
        private const val COLUMN_USER_ID = "userId"

    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create recipes table
        val createRecipesTable = """
            CREATE TABLE $TABLE_RECIPES (
                $COLUMN_RECIPE_ID TEXT PRIMARY KEY,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_FAVORITE INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createRecipesTable)

        // Create ingredients table
        val createIngredientsTable = """
            CREATE TABLE $TABLE_INGREDIENTS (
                $COLUMN_INGREDIENT_ID TEXT PRIMARY KEY,
                $COLUMN_RECIPE_ID_FK TEXT NOT NULL,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_QUANTITY REAL NOT NULL,
                $COLUMN_UNIT_MEASURE TEXT NOT NULL,
                FOREIGN KEY ($COLUMN_RECIPE_ID_FK) REFERENCES $TABLE_RECIPES ($COLUMN_RECIPE_ID)
                ON DELETE CASCADE
            )
        """.trimIndent()
        db.execSQL(createIngredientsTable)

        // Create local favorites table for offline storage
        val createLocalFavoritesTable = """
            CREATE TABLE $TABLE_LOCAL_FAVORITES (
                $COLUMN_USER_ID TEXT NOT NULL,
                $COLUMN_RECIPE_ID TEXT NOT NULL,
                PRIMARY KEY ($COLUMN_USER_ID, $COLUMN_RECIPE_ID)
            )
        """.trimIndent()
        db.execSQL(createLocalFavoritesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOCAL_FAVORITES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INGREDIENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECIPES")
        onCreate(db)
    }

    // Insert a new recipe with ingredients
    fun insertRecipe(recipe: Recipe): Long {
        val db = writableDatabase
        val recipeValues = ContentValues().apply {
            put(COLUMN_RECIPE_ID, recipe.recipeId)
            put(COLUMN_NAME, recipe.name)
            put(COLUMN_FAVORITE, if (recipe.favorite) 1 else 0)
        }
        val recipeId = db.insert(TABLE_RECIPES, null, recipeValues)

        if (recipeId != -1L) {
            // Insert ingredients
            for (ingredient in recipe.ingredients) {
                val ingredientValues = ContentValues().apply {
                    put(COLUMN_INGREDIENT_ID, ingredient.ingredientId)
                    put(COLUMN_RECIPE_ID_FK, recipe.recipeId)
                    put(COLUMN_NAME, ingredient.name)
                    put(COLUMN_QUANTITY, ingredient.quantity)
                    put(COLUMN_UNIT_MEASURE, ingredient.unitMeasure)
                }
                db.insert(TABLE_INGREDIENTS, null, ingredientValues)
            }
        }

        db.close()
        return recipeId
    }

    // Get all recipes with their ingredients
    fun getAllRecipes(): List<Recipe> {
        val recipes = mutableListOf<Recipe>()
        val db = readableDatabase

        val cursor = db.query(TABLE_RECIPES, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val recipeId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val favorite = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE)) == 1

            // Get ingredients for this recipe
            val ingredients = getIngredientsForRecipe(db, recipeId)

            recipes.add(Recipe(recipeId, name, ingredients, favorite))
        }
        cursor.close()
        db.close()
        return recipes
    }

    // Get ingredients for a specific recipe
    private fun getIngredientsForRecipe(db: SQLiteDatabase, recipeId: String): List<Ingredient> {
        val ingredients = mutableListOf<Ingredient>()
        val cursor = db.query(
            TABLE_INGREDIENTS, null, "$COLUMN_RECIPE_ID_FK = ?",
            arrayOf(recipeId), null, null, null
        )
        while (cursor.moveToNext()) {
            val ingredientId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENT_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY))
            val unitMeasure = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIT_MEASURE))

            ingredients.add(Ingredient(ingredientId, recipeId, name, quantity, unitMeasure))
        }
        cursor.close()
        return ingredients
    }

    // Update favorite status for a recipe
    fun updateFavoriteStatus(recipeId: String, isFavorite: Boolean): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FAVORITE, if (isFavorite) 1 else 0)
        }
        val rowsAffected = db.update(TABLE_RECIPES, values, "$COLUMN_RECIPE_ID = ?", arrayOf(recipeId))
        db.close()
        return rowsAffected
    }

    // Delete a recipe and its ingredients
    fun deleteRecipe(recipeId: String): Int {
        val db = writableDatabase
        val rowsAffected = db.delete(TABLE_RECIPES, "$COLUMN_RECIPE_ID = ?", arrayOf(recipeId))
        db.close()
        return rowsAffected
    }

    // Get a specific recipe by ID
    fun getRecipeById(recipeId: String): Recipe? {
        val db = readableDatabase
        val cursor = db.query(TABLE_RECIPES, null, "$COLUMN_RECIPE_ID = ?", arrayOf(recipeId), null, null, null)
        var recipe: Recipe? = null
        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val favorite = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE)) == 1
            val ingredients = getIngredientsForRecipe(db, recipeId)
            recipe = Recipe(recipeId, name, ingredients, favorite)
        }
        cursor.close()
        db.close()
        return recipe
    }

    // Save favorite offline
    fun saveFavoriteOffline(userId: String, recipeId: String, isFavorite: Boolean): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_RECIPE_ID, recipeId)
            put(COLUMN_FAVORITE, if (isFavorite) 1 else 0)
        }
        val rowId = db.insert(TABLE_LOCAL_FAVORITES, null, values)
        db.close()
        return rowId
    }

    // Get offline favorites for a user
    fun getOfflineFavorites(userId: String): List<String> {
        val favoriteRecipeIds = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_LOCAL_FAVORITES, null, "$COLUMN_USER_ID = ?",
            arrayOf(userId), null, null, null
        )
        while (cursor.moveToNext()) {
            val recipeId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_ID))
            val favorite = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE)) == 1
            if (favorite) {
                favoriteRecipeIds.add(recipeId)
            }
        }
        cursor.close()
        db.close()
        return favoriteRecipeIds
    }

    // Delete offline favorite
    fun deleteOfflineFavorite(userId: String, recipeId: String): Int {
        val db = writableDatabase
        val rowsAffected = db.delete(
            TABLE_LOCAL_FAVORITES, "$COLUMN_USER_ID = ? AND $COLUMN_RECIPE_ID = ?",
            arrayOf(userId, recipeId)
        )
        db.close()
        return rowsAffected
    }
}

// Example usage in an Activity
class RecipeActivity : AppCompatActivity() {
    private lateinit var dbHelper: RecipeDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = RecipeDatabaseHelper(this)

        // Example: Insert a recipe
        val ingredient1 = Ingredient("1", "1", "Flour", 2.0, "cups")
        val ingredient2 = Ingredient("2", "1", "Sugar", 1.0, "cup")
        val recipe = Recipe("1", "Pancakes", listOf(ingredient1, ingredient2), false)
        dbHelper.insertRecipe(recipe)

        // Example: Get all recipes
        val recipes = dbHelper.getAllRecipes()
        // Display recipes...

        // Example: Update favorite
        dbHelper.updateFavoriteStatus("1", true)

        // Example: Delete recipe
        dbHelper.deleteRecipe("1")

        // Example: Save favorite offline
        val userId = "user123"
        dbHelper.saveFavoriteOffline(userId, "1", true)

        // Example: Get offline favorites
        val offlineFavorites = dbHelper.getOfflineFavorites(userId)
        // Display offline favorites...

        // Example: Delete offline favorite
        dbHelper.deleteOfflineFavorite(userId, "1")
    }
}