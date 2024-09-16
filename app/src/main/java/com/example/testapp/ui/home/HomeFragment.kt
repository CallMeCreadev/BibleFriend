package com.example.testapp.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.testapp.R
import com.example.testapp.data.AppDatabase
import com.example.testapp.data.Bible
import com.example.testapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    private val bibleVerseDao by lazy {
        AppDatabase.getDatabase(requireContext()).bibleVerseDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        textView.text = """
            Hello! I am Bible Friend
            This app uses the King James Bible and can function offline
            Everything is built into the app itself
            A personal database is contained within the app on your phone
            This database saves your favorites list, and bookmarks your Bible
            The custom database allows you to search the bible for key phrases
            You can copy text from your bible and use the search function to add it to your favorites
            Included is custom AI model that finds verses based on your prompts
            There are no servers or API calls meaning your data is 100% private
            App will auto-close once after first Install/Opening to ensure tables & AI are loaded properly.
        """.trimIndent()

        val fontSizeSpinner: Spinner = binding.fontSizeSpinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.font_size_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            fontSizeSpinner.adapter = adapter
        }

        // Load saved font size
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val savedFontSize = sharedPreferences.getFloat("font_size", 20f)
        fontSizeSpinner.setSelection(getFontSizePosition(savedFontSize))
        textView.textSize = savedFontSize

        fontSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedSize = when (position) {
                    0 -> 16f
                    1 -> 20f
                    2 -> 24f
                    else -> 20f
                }
                // Save font size to SharedPreferences
                sharedPreferences.edit().putFloat("font_size", selectedSize).apply()
                // Apply the font size immediately
                homeViewModel.setFontSize(selectedSize)
                applyGlobalFontSize(selectedSize)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        homeViewModel.fontSize.observe(viewLifecycleOwner) {
            textView.textSize = it
        }

        // Check if the Bible text has already been parsed and inserted
        val isBibleParsed = sharedPreferences.getBoolean("is_bible_parsed", false)

        if (!isBibleParsed) {
            parseAndInsertBibleText()
            sharedPreferences.edit().putBoolean("is_bible_parsed", true).apply()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun parseAndInsertBibleText() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val bibleText = readTextFromAsset("bible.txt")
                val regex = Regex("(\\n\\w+\\n)|\\d+:\\d+")
                val matches = regex.findAll(bibleText).toList()

                val verses = mutableListOf<Bible>()
                var currentChapter = ""
                var lastIndex = 0

                for (match in matches) {
                    if (match.value.matches(Regex("\\n\\w+\\n"))) {
                        currentChapter = match.value.trim()
                    } else {
                        val verseLabel = match.value
                        val nextMatchIndex = matches.indexOf(match) + 1
                        val nextMatch = if (nextMatchIndex < matches.size) matches[nextMatchIndex] else null
                        val verseText = bibleText.substring(match.range.last + 1, nextMatch?.range?.first ?: bibleText.length).trim()
                        verses.add(Bible(chapter = currentChapter, verse = verseLabel, text = verseText))
                    }
                    lastIndex = match.range.last + 1
                }

                // Uncomment to insert into the database
                // bibleVerseDao.insertAll(verses)
            }
        }
    }

    private suspend fun readTextFromAsset(fileName: String): String {
        return withContext(Dispatchers.IO) {
            val inputStream = requireContext().assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.use { it.readText() }
        }
    }

    private fun applyGlobalFontSize(fontSize: Float) {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putFloat("font_size", fontSize)
            apply()
        }
    }

    private fun getFontSizePosition(fontSize: Float): Int {
        return when (fontSize) {
            16f -> 0
            20f -> 1
            24f -> 2
            else -> 1
        }
    }
}
