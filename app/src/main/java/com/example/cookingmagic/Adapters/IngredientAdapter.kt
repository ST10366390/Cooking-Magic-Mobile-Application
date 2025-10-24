package com.example.cookingmagic.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cookingmagic.Dataclasses.Ingredient
import com.example.cookingmagic.R

class IngredientAdapter(
    private val ingredients: MutableList<Ingredient> = mutableListOf()
) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.ingredientNameTextView)
        private val quantityTextView: TextView = itemView.findViewById(R.id.quantityValueTextView)
        private val unitTextView: TextView = itemView.findViewById(R.id.unitMeasureTextView)

        fun bind(ingredient: Ingredient) {
            // Set ingredient name
            nameTextView.text = ingredient.name

            // Format quantity: show as integer if no decimal, otherwise one decimal place
            val formattedQuantity = if (ingredient.quantity % 1 == 0.0) {
                ingredient.quantity.toInt().toString()
            } else {
                "%.1f".format(ingredient.quantity)
            }

            // Set quantity and unit
            quantityTextView.text = formattedQuantity
            unitTextView.text = ingredient.unitMeasure
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ingredient_layout_card, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(ingredients[position])
    }

    override fun getItemCount(): Int = ingredients.size

    // Add a new ingredient to the list
    fun addIngredient(ingredient: Ingredient) {
        ingredients.add(ingredient)
        notifyItemInserted(ingredients.size - 1)
    }

    // Remove an ingredient at the specified position
    fun removeIngredientAt(position: Int) {
        if (position in 0 until ingredients.size) {
            ingredients.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, ingredients.size)
        }
    }

    // Clear all ingredients
    fun clearAll() {
        val size = ingredients.size
        ingredients.clear()
        notifyItemRangeRemoved(0, size)
    }

    // Get all ingredients as a list
    fun getAllIngredients(): List<Ingredient> = ingredients.toList()
}