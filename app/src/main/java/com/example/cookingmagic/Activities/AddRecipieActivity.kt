package com.example.cookingmagic.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookingmagic.Adapters.IngredientAdapter
import com.example.cookingmagic.Dataclasses.Ingredient
import com.example.cookingmagic.databinding.ActivityAddRecipieBinding
import com.example.cookingmagic.R
import com.google.firebase.database.FirebaseDatabase

class AddRecipieActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddRecipieBinding
    private lateinit var ingredientAdapter: IngredientAdapter
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddRecipieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup RecyclerView
        ingredientAdapter = IngredientAdapter()
        binding.ingredientsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AddRecipieActivity)
            adapter = ingredientAdapter
        }

        // Add Ingredient Button
        binding.addIngredientButton.setOnClickListener {
            val name = binding.ingredientNameEditText.text.toString().trim()
            val quantityStr = binding.quantityEditText.text.toString().trim()
            val unit = binding.unitEditText.text.toString().trim()

            if (name.isNotEmpty() && quantityStr.isNotEmpty() && unit.isNotEmpty()) {
                val quantity = quantityStr.toDoubleOrNull()
                if (quantity != null && quantity > 0) {
                    val ingredient = Ingredient(
                        ingredientId = "", // Will be set when saving
                        recipeId = "",     // Will be set when saving
                        name = name,
                        quantity = quantity,
                        unitMeasure = unit
                    )
                    ingredientAdapter.addIngredient(ingredient)

                    // Clear input fields
                    binding.ingredientNameEditText.text?.clear()
                    binding.quantityEditText.text?.clear()
                    binding.unitEditText.text?.clear()

                    // Scroll to bottom of ScrollView
                    binding.scrollView.post {
                        binding.scrollView.fullScroll(android.widget.ScrollView.FOCUS_DOWN)
                    }
                } else {
                    Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all ingredient fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Done Button
        binding.doneButton.setOnClickListener {
            val recipeName = binding.recipeNameEditText.text.toString().trim()
            val allIngredients = ingredientAdapter.getAllIngredients()
            if (recipeName.isNotEmpty() && allIngredients.isNotEmpty()) {
                saveRecipeToFirebase(recipeName, allIngredients)
            } else {
                Toast.makeText(this, "Please enter a recipe name and at least one ingredient", Toast.LENGTH_SHORT).show()
            }
        }

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

    private fun saveRecipeToFirebase(name: String, ingredients: List<Ingredient>) {
        // Generate a unique recipe ID
        val recipeRef = database.child("recipes").push()
        val recipeId = recipeRef.key ?: return

        // Prepare recipe data
        val recipeData = mapOf(
            "name" to name
        )

        // Save recipe name
        recipeRef.setValue(recipeData)
            .addOnSuccessListener {
                // Save each ingredient with its own ID
                val ingredientsRef = recipeRef.child("ingredients")
                ingredients.forEach { ingredient ->
                    val ingredientRef = ingredientsRef.push()
                    val ingredientData = mapOf(
                        "name" to ingredient.name,
                        "quantity" to ingredient.quantity,
                        "unitMeasure" to ingredient.unitMeasure
                    )
                    ingredientRef.setValue(ingredientData)
                }
                Toast.makeText(this, "Recipe saved successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save recipe", Toast.LENGTH_SHORT).show()
            }
    }
}