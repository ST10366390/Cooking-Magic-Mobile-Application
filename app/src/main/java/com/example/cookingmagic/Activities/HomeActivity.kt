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
import com.example.cookingmagic.Adapters.StoryAdapter
import com.example.cookingmagic.Dataclasses.Ingredient
import com.example.cookingmagic.Dataclasses.Recipe
import com.example.cookingmagic.Dataclasses.Story
import com.example.cookingmagic.R
import com.example.cookingmagic.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.view.inputmethod.EditorInfo
import android.widget.Toast

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var storyAdapter: StoryAdapter
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val allRecipes = mutableListOf<Recipe>()
    private val allStories = mutableListOf<Story>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check if user is signed in
        val user = auth.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Set welcome text with user display name
        database.child("users").child(user.uid).child("displayName")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val displayName = snapshot.getValue(String::class.java) ?: "User"
                    binding.welcomeTextView.text = "Welcome, $displayName!"
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.welcomeTextView.text = "Welcome Back!"
                    Toast.makeText(this@HomeActivity, "Failed to load user name: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })

        // Setup Recipes RecyclerView
        recipeAdapter = RecipeAdapter()
        binding.recipesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = recipeAdapter
        }

        // Setup Stories RecyclerView
        storyAdapter = StoryAdapter()
        binding.storiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = storyAdapter
        }

        // Fetch data from Firebase
        fetchRecipes()
        fetchStories()

        // Setup Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on Home, no action needed
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

        // Setup Search Bar Click to Navigate
        binding.searchEditText.setOnClickListener {
            startActivity(Intent(this, ViewRecipiesActivity::class.java))

        }

        // Setup Search Action
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchEditText.text.toString().trim()
                filterRecipes(query)
                true
            } else {
                false
            }
        }

        // Account Icon Click
        binding.accountIcon.setOnClickListener {
            startActivity(Intent(this, OptionsActivity::class.java))
            finish()
        }
    }

    private fun fetchRecipes() {
        database.child("recipes").addValueEventListener(object : ValueEventListener {
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
                Toast.makeText(this@HomeActivity, "Failed to load recipes: ${error.message}", Toast.LENGTH_SHORT).show()
                recipeAdapter.updateRecipes(emptyList())
                updateEmptyState()
            }
        })
    }

    private fun fetchStories() {
        database.child("stories").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allStories.clear()
                for (storySnapshot in snapshot.children) {
                    val storyId = storySnapshot.key ?: continue
                    val title = storySnapshot.child("title").getValue(String::class.java) ?: continue
                    val imageAddress = storySnapshot.child("imageAddress").getValue(String::class.java) ?: continue
                    allStories.add(Story(storyId, title, imageAddress))
                }
                if (allStories.isEmpty()) {
                    // Fallback to placeholder stories
                    allStories.addAll(
                        listOf(
                            Story("story1", "Quick Tips", "drawable://tip_one"),
                            Story("story2", "Baking Hacks", "drawable://tip_two"),
                            Story("story3", "Pap", "drawable://tip_three"),
                            Story("story4", "Meal ", "drawable://tip_four"),
                            Story("story5", "Prep", "drawable://tip_5")
                        )
                    )
                }
                storyAdapter.updateStories(allStories)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Failed to load stories: ${error.message}", Toast.LENGTH_SHORT).show()
                // Fallback to placeholder stories
                allStories.addAll(
                    listOf(
                        Story("story1", "Quick Tips", "drawable://tip_one"),
                        Story("story2", "Baking Hacks", "drawable://tip_two"),
                        Story("story3", "Pap", "drawable://tip_three"),
                        Story("story4", "Meal ", "drawable://tip_four"),
                        Story("story5", "Prep", "drawable://tip_5")
                    )
                )
                storyAdapter.updateStories(allStories)
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