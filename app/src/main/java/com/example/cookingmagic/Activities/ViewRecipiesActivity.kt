package com.example.cookingmagic.Activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookingmagic.Adapters.RecipeAdapter
import com.example.cookingmagic.Dataclasses.Ingredient
import com.example.cookingmagic.Dataclasses.Recipe
import com.example.cookingmagic.R
import com.example.cookingmagic.databinding.ActivityViewRecipiesBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.widget.Toast

class ViewRecipiesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewRecipiesBinding
    private lateinit var recipeAdapter: RecipeAdapter
    private val database = FirebaseDatabase.getInstance().reference.child("recipes")
    private val allRecipes = mutableListOf<Recipe>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewRecipiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup RecyclerView
        recipeAdapter = RecipeAdapter()
        binding.recipesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ViewRecipiesActivity)
            adapter = recipeAdapter
        }

        // Fetch recipes from Firebase
        fetchRecipes()

        // Setup Search
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed
            }

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                filterRecipes(query)
            }
        })

        // Setup Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))

                    true
                }
                R.id.nav_report -> {
                    // Already on ViewRecipiesActivity, no action needed
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, AddRecipieActivity::class.java))

                    true
                }
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoriteRecipies::class.java))

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

    private fun fetchRecipes() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allRecipes.clear()
                for (recipeSnapshot in snapshot.children) {
                    val recipeId = recipeSnapshot.key ?: continue
                    val name = recipeSnapshot.child("name").getValue(String::class.java) ?: continue
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
                    allRecipes.add(Recipe(recipeId, name, ingredients, false))
                }
                recipeAdapter.updateRecipes(allRecipes)
                updateEmptyState()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewRecipiesActivity, "Failed to load recipes: ${error.message}", Toast.LENGTH_SHORT).show()
                recipeAdapter.updateRecipes(emptyList())
                updateEmptyState()
            }
        })
    }

    private fun filterRecipes(query: String) {
        val filteredRecipes = if (query.isEmpty()) {
            allRecipes
        } else {
            allRecipes.filter { it.name.contains(query, ignoreCase = true) }
        }
        recipeAdapter.updateRecipes(filteredRecipes)
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (recipeAdapter.itemCount == 0) {
            binding.emptyListImageView.visibility = View.VISIBLE
            binding.recipesRecyclerView.visibility = View.GONE
        } else {
            binding.emptyListImageView.visibility = View.GONE
            binding.recipesRecyclerView.visibility = View.VISIBLE
        }
    }
}