package com.example.cookingmagic.Activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cookingmagic.databinding.ActivityOptionsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.widget.Toast

class OptionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOptionsBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOptionsBinding.inflate(layoutInflater)
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
            Toast.makeText(this, "Please sign in to access options", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set user initials from Firebase Auth
        val displayName = user.displayName
        binding.userInitialsText.text = if (displayName.isNullOrBlank()) {
            "U"
        } else {
            extractInitials(displayName)
        }

        // Fetch recipe and favorite counts from Firebase
        fetchRecipesAddedCount(user.uid)
        fetchFavoritesCount(user.uid)

        // Back button click
        binding.backButton.setOnClickListener {
            finish()
        }

        // View Recipes option click
        binding.viewRecipesOption.setOnClickListener {
            val intent = Intent(this, ViewRecipiesActivity::class.java)
            startActivity(intent)
        }

        // View Favorites option click
        binding.viewFavoritesOption.setOnClickListener {
            val intent = Intent(this, FavoriteRecipies::class.java)
            startActivity(intent)
        }

        // Dark Mode option click (placeholder)
        binding.darkModeOption.setOnClickListener {
            // TODO: Implement dark mode toggle
            startActivity(Intent(this, SearchRecipeActivity::class.java))
            Toast.makeText(this, "Dark mode toggle coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Language Settings option click (placeholder)
        binding.languageSettingsOption.setOnClickListener {
            // TODO: Implement language settings navigation or dialog

            Toast.makeText(this, "Language settings coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Log Out option click
        binding.logOutOption.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun extractInitials(fullName: String): String {
        val words = fullName.trim().split("\\s+".toRegex())
        val initials = words.mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")
        return if (initials.isNotEmpty()) initials else "SA"
    }

    private fun fetchRecipesAddedCount(userId: String) {
        database.child("users").child(userId).child("recipesAdded")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount.toInt()
                    binding.recipesAddedTextView.text = "Recipes Added: $count"
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.recipesAddedTextView.text = "Recipes Added: 0"
                    Toast.makeText(this@OptionsActivity, "Failed to load recipes count: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchFavoritesCount(userId: String) {
        database.child("users").child(userId).child("favorites")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount.toInt()
                    binding.favoritesTextView.text = "Favorite Recipes: $count"
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.favoritesTextView.text = "Favorite Recipes: 0"
                    Toast.makeText(this@OptionsActivity, "Failed to load favorites count: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}