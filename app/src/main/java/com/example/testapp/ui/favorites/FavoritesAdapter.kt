package com.example.testapp.ui.favorites

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testapp.R
import com.example.testapp.data.FavoriteVerse

class FavoritesAdapter(
    private var favoriteVerses: List<FavoriteVerse>,
    private val onVerseChecked: (FavoriteVerse, Boolean) -> Unit,
    private val context: Context // Add context to the constructor
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    private val selectedVerses = mutableSetOf<FavoriteVerse>()
    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val fontSize = sharedPreferences.getFloat("font_size", 20f) // Default to 20sp

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val verseText: TextView = itemView.findViewById(R.id.verseText)
        val verseLabel: TextView = itemView.findViewById(R.id.verseLabel)
        val chapterTitle: TextView = itemView.findViewById(R.id.chapterTitle)
        val checkBox: CheckBox = itemView.findViewById(R.id.verseCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val favoriteVerse = favoriteVerses[position]
        holder.verseText.text = favoriteVerse.text
        holder.verseLabel.text = favoriteVerse.verse
        holder.chapterTitle.text = favoriteVerse.chapter

        // Apply the global font size
        holder.verseText.textSize = fontSize
        holder.verseLabel.textSize = fontSize - 2  // Slightly smaller than verseText
        holder.chapterTitle.textSize = fontSize + 2  // Slightly larger than verseText

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = selectedVerses.contains(favoriteVerse)
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onVerseChecked(favoriteVerse, isChecked)
            if (isChecked) {
                selectedVerses.add(favoriteVerse)
            } else {
                selectedVerses.remove(favoriteVerse)
            }
        }
    }

    override fun getItemCount(): Int {
        return favoriteVerses.size
    }

    fun updateData(newFavoriteVerses: List<FavoriteVerse>) {
        favoriteVerses = newFavoriteVerses
        selectedVerses.clear()
        notifyDataSetChanged()
    }

    fun getSelectedVerses(): List<FavoriteVerse> {
        return selectedVerses.toList()
    }

    fun clearSelection() {
        selectedVerses.clear()
        notifyDataSetChanged()
    }
}
