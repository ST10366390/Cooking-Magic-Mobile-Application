package com.example.cookingmagic.Activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cookingmagic.R
import com.example.cookingmagic.databinding.ActivityTipFiveBinding
import com.example.cookingmagic.Dataclasses.Story
import android.widget.Toast

class TipFiveActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTipFiveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTipFiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve Story from Intent
        /*val story = intent.getParcelableExtra<Story>("STORY")
        if (story == null) {
            Toast.makeText(this, "Error: No tip data provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set UI elements
        binding.tipTitleTextView.text = story.title
        val drawableId = when (story.imageAddress) {
            "drawable://tip_5" -> R.drawable.tip_5
            else -> R.drawable.banner
        }
        binding.tipImageView.setImageResource(drawableId)
        binding.tipDescriptionTextView.text = story.description ?: "Essential prep techniques for every home chef."
*/
        // Back button
        binding.backButton.setOnClickListener {
            finish()
        }
    }
}