package com.example.cookingmagic.Activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cookingmagic.api.Recipe
import com.example.cookingmagic.api.RetrofitClient
import com.example.cookingmagic.databinding.ActivitySearchRecipeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchRecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchRecipeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back button click
        binding.backButton.setOnClickListener {
            finish()
        }

        // Search button click
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchRecipes(query)
            } else {
                Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show()
                binding.resultsTextView.text = "No results yet. Enter a query to search for recipes."
            }
        }
    }

    private fun searchRecipes(query: String) {
        binding.resultsTextView.text = "Searching for '$query'..."
        val call = RetrofitClient.apiService.searchRecipes(query)
        call.enqueue(object : Callback<List<Recipe>> {
            override fun onResponse(call: Call<List<Recipe>>, response: Response<List<Recipe>>) {
                if (response.isSuccessful) {
                    val recipes = response.body() ?: emptyList()
                    if (recipes.isEmpty()) {
                        binding.resultsTextView.text = "No recipes found for '$query'."
                        Toast.makeText(this@SearchRecipeActivity, "No recipes found", Toast.LENGTH_SHORT).show()
                    } else {
                        val resultText = recipes.joinToString("\n\n") { recipe ->
                            "Recipe: ${recipe.title}\n" +
                                    "Ingredients: ${recipe.ingredients.split(",")           // split by comma
                                        .map { it.trim() }    // remove extra spaces
                                        .filter { it.isNotBlank() }}\n" +
                                    "Servings: ${recipe.servings}\n" +
                                    "Instructions: ${recipe.instructions}"
                        }
                        binding.resultsTextView.text = resultText
                    }
                } else {
                    binding.resultsTextView.text = "Failed to fetch recipes for '$query'."
                    Toast.makeText(this@SearchRecipeActivity, "Failed to fetch recipes: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                binding.resultsTextView.text = "Error fetching recipes for '$query'."
                Toast.makeText(this@SearchRecipeActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.d("API","Error: ${t.message}")
            }
        })
    }
}