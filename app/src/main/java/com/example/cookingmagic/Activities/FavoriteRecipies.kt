package com.example.cookingmagic.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookingmagic.Adapters.RecipeAdapter
import com.example.cookingmagic.Dataclasses.Ingredient
import com.example.cookingmagic.Dataclasses.Recipe
import com.example.cookingmagic.databinding.ActivityFavoriteRecipiesBinding
import com.example.cookingmagic.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.widget.Toast

class FavoriteRecipies : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteRecipiesBinding
    private lateinit var recipeAdapter: RecipeAdapter
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFavoriteRecipiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Update heading to "Favorite Recipes"
        binding.allRecipesTextView.text = "Favorite Recipes"

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check if user is signed in
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Setup RecyclerView
        recipeAdapter = RecipeAdapter()
        binding.recipesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@FavoriteRecipies)
            adapter = recipeAdapter
        }

        // Fetch favorite recipes from Firebase
        fetchFavoriteRecipes()

        // Setup Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))

                    true
                }
                R.id.nav_report -> {
                    startActivity(Intent(this, ViewRecipiesActivity::class.java))

                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, AddRecipieActivity::class.java))

                    true
                }
                R.id.nav_favorites -> {
                    // Already on Favorites, no action needed
                    true
                }
                R.id.nav_options -> {
                    startActivity(Intent(this, OptionsActivity::class.java))

                    true
                }
                else -> false
            }
        }
    }

    private fun fetchFavoriteRecipes() {
        val userId = auth.currentUser?.uid ?: return
        database.child("users").child(userId).child("favorites")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favoriteRecipeIds = snapshot.children.mapNotNull { it.key }
                    if (favoriteRecipeIds.isEmpty()) {
                        updateUI(emptyList())
                        return
                    }

                    // Fetch details for favorite recipes
                    database.child("recipes").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(recipesSnapshot: DataSnapshot) {
                            val recipes = mutableListOf<Recipe>()
                            for (recipeId in favoriteRecipeIds) {
                                val recipeSnapshot = recipesSnapshot.child(recipeId)
                                val name = recipeSnapshot.child("name").getValue(String::class.java)
                                if (name != null) {
                                    val ingredients = mutableListOf<Ingredient>()
                                    recipeSnapshot.child("ingredients").children.forEach { ingredientSnapshot ->
                                        val ingredientId = ingredientSnapshot.key ?: ""
                                        val ingredientName = ingredientSnapshot.child("name").getValue(String::class.java) ?: ""
                                        val quantity = ingredientSnapshot.child("quantity").getValue(Double::class.java) ?: 0.0
                                        val unitMeasure = ingredientSnapshot.child("unitMeasure").getValue(String::class.java) ?: ""
                                        ingredients.add(
                                            Ingredient(
                                                ingredientId = ingredientId,
                                                recipeId = recipeId,
                                                name = ingredientName,
                                                quantity = quantity,
                                                unitMeasure = unitMeasure
                                            )
                                        )
                                    }
                                    recipes.add(Recipe(recipeId, name, ingredients,false))
                                }
                            }
                            updateUI(recipes)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@FavoriteRecipies, "Failed to load favorite recipes: ${error.message}", Toast.LENGTH_SHORT).show()
                            updateUI(emptyList())
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@FavoriteRecipies, "Failed to load favorites: ${error.message}", Toast.LENGTH_SHORT).show()
                    updateUI(emptyList())
                }
            })
    }

    private fun updateUI(recipes: List<Recipe>) {
        recipeAdapter.updateRecipes(recipes)
        binding.emptyListImageView.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
        binding.recipesRecyclerView.visibility = if (recipes.isEmpty()) View.GONE else View.VISIBLE
    }
}