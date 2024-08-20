package com.example.testapp.ui.question

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testapp.R

class VerseAdapter(
    private var verses: List<Verse>,
    private val onVerseChecked: (Verse, Boolean) -> Unit,
    private val context: Context // Add context to the constructor
) : RecyclerView.Adapter<VerseAdapter.VerseViewHolder>() {

    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val fontSize = sharedPreferences.getFloat("font_size", 20f) // Default to 20sp

    inner class VerseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chapterTextView: TextView = itemView.findViewById(R.id.chapterTextView)
        val verseTextView: TextView = itemView.findViewById(R.id.verseTextView)
        val textTextView: TextView = itemView.findViewById(R.id.textTextView)
        val checkBox: CheckBox = itemView.findViewById(R.id.verseCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_verse, parent, false)
        return VerseViewHolder(view)
    }

    override fun onBindViewHolder(holder: VerseViewHolder, position: Int) {
        val verse = verses[position]
        holder.chapterTextView.text = verse.chapter
        holder.verseTextView.text = verse.verseNumber
        holder.textTextView.text = verse.text

        // Apply the global font size
        holder.chapterTextView.textSize = fontSize
        holder.verseTextView.textSize = fontSize - 2  // Slightly smaller than chapterTextView
        holder.textTextView.textSize = fontSize - 2  // Slightly smaller than chapterTextView

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = false
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onVerseChecked(verse, isChecked)
        }
    }

    override fun getItemCount(): Int {
        return verses.size
    }

    fun updateData(newVerses: List<Verse>) {
        verses = newVerses
        notifyDataSetChanged()
    }
}
