package com.example.cookingmagic.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cookingmagic.Activities.TipOneActivity
import com.example.cookingmagic.Activities.TipTwoActivity
import com.example.cookingmagic.Activities.TipThreeActivity
import com.example.cookingmagic.Activities.TipFourActivity
import com.example.cookingmagic.Activities.TipFiveActivity
import com.example.cookingmagic.Dataclasses.Story
import com.example.cookingmagic.R

class StoryAdapter(
    private val stories: MutableList<Story> = mutableListOf()
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val storyImageView: ImageView = itemView.findViewById(R.id.storyImageView)
        private val storyTitleTextView: TextView = itemView.findViewById(R.id.storyTitleTextView)

        fun bind(story: Story) {
            storyTitleTextView.text = story.title
            val drawableId = when (story.imageAddress) {
                "drawable://tip_one" -> R.drawable.tip_one
                "drawable://tip_two" -> R.drawable.tip_two
                "drawable://tip_three" -> R.drawable.tip_three
                "drawable://tip_four" -> R.drawable.tip_four
                "drawable://tip_5" -> R.drawable.tip_5
                else -> R.drawable.banner // Fallback for invalid or URL-based imageAddress
            }
            storyImageView.setImageResource(drawableId)

            // Navigate to specific activity when image is clicked
            storyImageView.setOnClickListener {
                val intent = when (story.storyId) {
                    "story1" -> Intent(itemView.context, TipOneActivity::class.java)
                    "story2" -> Intent(itemView.context, TipTwoActivity::class.java)
                    "story3" -> Intent(itemView.context, TipThreeActivity::class.java)
                    "story4" -> Intent(itemView.context, TipFourActivity::class.java)
                    "story5" -> Intent(itemView.context, TipFiveActivity::class.java)
                    else -> return@setOnClickListener // No navigation for unknown storyId
                }
                intent.putExtra("STORY", story.title)
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(stories[position])
    }

    override fun getItemCount(): Int = stories.size

    fun updateStories(newStories: List<Story>) {
        stories.clear()
        stories.addAll(newStories)
        notifyDataSetChanged()
    }

    fun addStory(story: Story) {
        stories.add(story)
        notifyItemInserted(stories.size - 1)
    }

    fun clearAll() {
        val size = stories.size
        stories.clear()
        notifyItemRangeRemoved(0, size)
    }
}