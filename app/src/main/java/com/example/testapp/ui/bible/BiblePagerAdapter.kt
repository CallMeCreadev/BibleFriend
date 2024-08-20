package com.example.testapp.ui.bible

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testapp.R

class BiblePagerAdapter(private val pages: List<String>, private val context: Context) : RecyclerView.Adapter<BiblePagerAdapter.BibleViewHolder>() {

    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val fontSize = sharedPreferences.getFloat("font_size", 20f) // Default to 20sp

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BibleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.page_bible, parent, false)
        return BibleViewHolder(view)
    }

    override fun onBindViewHolder(holder: BibleViewHolder, position: Int) {
        holder.bind(pages[position], fontSize)
    }

    override fun getItemCount(): Int = pages.size

    class BibleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.textBiblePage)

        fun bind(text: String, fontSize: Float) {
            textView.text = text
            textView.setTextIsSelectable(true)
            textView.textSize = fontSize
        }
    }
}
