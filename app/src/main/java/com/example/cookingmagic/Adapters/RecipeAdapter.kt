package com.example.cookingmagic.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cookingmagic.Dataclasses.Recipe
import com.example.cookingmagic.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.widget.Toast

class RecipeAdapter(
    private val recipes: MutableList<Recipe> = mutableListOf()
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.recipeNameTextView)
        private val favoriteIcon: ImageView = itemView.findViewById(R.id.favoriteIcon)
        private val ingredientsRecyclerView: RecyclerView = itemView.findViewById(R.id.ingredientsRecyclerView)
        private val ingredientAdapter = IngredientAdapter()

        fun bind(recipe: Recipe) {
            nameTextView.text = recipe.name
            ingredientsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = ingredientAdapter
            }
            ingredientAdapter.clearAll()
            recipe.ingredients.forEach { ingredientAdapter.addIngredient(it) }

            // Check favorite status
            val userId = auth.currentUser?.uid
            if (userId != null) {
                database.child("users").child(userId).child("favorites").child(recipe.recipeId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            favoriteIcon.isSelected = snapshot.exists()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(itemView.context, "Error checking favorite status: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })

                // Toggle favorite on click
                favoriteIcon.setOnClickListener {
                    val isFavorite = favoriteIcon.isSelected
                    favoriteIcon.isSelected = !isFavorite
                    updateFavoriteStatus(recipe.recipeId, !isFavorite, recipe.name)
                }
            } else {
                favoriteIcon.isEnabled = false
                favoriteIcon.isSelected = false
                favoriteIcon.setOnClickListener {
                    Toast.makeText(itemView.context, "Please sign in to favorite recipes", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun updateFavoriteStatus(recipeId: String, isFavorite: Boolean, recipeName: String) {
            val userId = auth.currentUser?.uid ?: return
            val favoriteRef = database.child("users").child(userId).child("favorites").child(recipeId)
            if (isFavorite) {
                favoriteRef.setValue(true).addOnSuccessListener {
                    Toast.makeText(itemView.context, "Added $recipeName to favorites", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    favoriteIcon.isSelected = false // Revert UI on failure
                    Toast.makeText(itemView.context, "Failed to add $recipeName to favorites", Toast.LENGTH_SHORT).show()
                }
            } else {
                favoriteRef.removeValue().addOnSuccessListener {
                    Toast.makeText(itemView.context, "Removed $recipeName from favorites", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    favoriteIcon.isSelected = true // Revert UI on failure
                    Toast.makeText(itemView.context, "Failed to remove $recipeName from favorites", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recipe_layout_card, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes.clear()
        recipes.addAll(newRecipes)
        notifyDataSetChanged()
    }

    fun addRecipe(recipe: Recipe) {
        recipes.add(recipe)
        notifyItemInserted(recipes.size - 1)
    }

    fun removeRecipeAt(position: Int) {
        if (position in 0 until recipes.size) {
            recipes.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, recipes.size)
        }
    }

    fun clearAll() {
        val size = recipes.size
        recipes.clear()
        notifyItemRangeRemoved(0, size)
    }
}