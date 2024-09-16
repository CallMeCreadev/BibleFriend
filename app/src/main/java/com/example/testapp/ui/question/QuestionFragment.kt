package com.example.testapp.ui.question

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testapp.data.AppDatabase
import com.example.testapp.data.FavoriteVerse
import com.example.testapp.databinding.FragmentQuestionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.ceil
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class QuestionFragment : Fragment() {

    private var _binding: FragmentQuestionBinding? = null
    private val binding get() = _binding!!

    private val selectedVerses = mutableSetOf<Verse>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val questionViewModel = ViewModelProvider(this).get(QuestionViewModel::class.java)

        _binding = FragmentQuestionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val promptEditText: EditText = binding.promptEditText
        val fetchButton: Button = binding.fetchButton
        val searchButton: Button = binding.searchButton
        val versesRecyclerView: RecyclerView = binding.versesRecyclerView
        val addToFavoritesButton: Button = binding.addToFavoritesButton
        val progressBar: ProgressBar = binding.progressBar

        versesRecyclerView.layoutManager = LinearLayoutManager(context)

        fetchButton.setOnClickListener {
            val prompt = promptEditText.text.toString()
            progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    val verses = withContext(Dispatchers.IO) {
                        fetchMatchingVerses(requireContext(), prompt)
                    }
                    val adapter = VerseAdapter(verses, ::onVerseChecked, requireContext())
                    versesRecyclerView.adapter = adapter
                    versesRecyclerView.scrollToPosition(0)
                } catch (e: Exception) {
                    // Handle exceptions
                } finally {
                    progressBar.visibility = View.GONE
                }
            }
        }

        searchButton.setOnClickListener {
            val prompt = promptEditText.text.toString()
            progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    val verses = withContext(Dispatchers.IO) {
                        searchVerses(requireContext(), prompt)
                    }
                    val adapter = VerseAdapter(verses, ::onVerseChecked, requireContext())
                    versesRecyclerView.adapter = adapter
                    versesRecyclerView.scrollToPosition(0)
                } catch (e: Exception) {
                    // Handle exceptions
                } finally {
                    progressBar.visibility = View.GONE
                }
            }
        }

        addToFavoritesButton.setOnClickListener {
            addToFavorites(requireContext(), selectedVerses.toList())
            selectedVerses.clear()
            addToFavoritesButton.visibility = View.GONE
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onVerseChecked(verse: Verse, isChecked: Boolean) {
        if (isChecked) {
            selectedVerses.add(verse)
        } else {
            selectedVerses.remove(verse)
        }
        binding.addToFavoritesButton.visibility = if (selectedVerses.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun fetchMatchingVerses(context: Context, prompt: String, topN: Int = 6, selectN: Int = 6): List<Verse> {
        // Trim the related words set to the first 10 words
        val relatedWordsSet = HashSet(prompt.lowercase().split(" ").take(10))

        val dbName = "bible.db"
        val dbPath = context.getDatabasePath(dbName).absolutePath
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

        val cursor = db.rawQuery("SELECT bible_book, Verse_Number, Verse, spacy_gensim_keys FROM Bible", null)

        val matchingVerses = mutableListOf<Quadruple<String, String, String, Double>>()

        if (cursor.moveToFirst()) {
            do {
                val chapter = cursor.getString(0)
                val verseNumber = cursor.getString(1)
                val verse = cursor.getString(2)
                val spacyGensimKeys = cursor.getString(3)

                if (!spacyGensimKeys.isNullOrEmpty()) {
                    val verseRelatedWordsSet = HashSet(spacyGensimKeys.lowercase().split(", "))
                    val verseWordsList = verse.lowercase().split(" ")
                    val combinedWordsSet = verseRelatedWordsSet.union(verseWordsList)
                    val commonWordsWithSpacy = relatedWordsSet.intersect(verseRelatedWordsSet).size
                    val commonWordsWithVerse = relatedWordsSet.intersect(verseWordsList.toSet()).size
                    var totalCommonWords = commonWordsWithSpacy + (4 * commonWordsWithVerse)
                    val totalWords = combinedWordsSet.size

                    var bonus = 0

                    // Perform the computationally expensive bonus check only if commonWordsWithVerse >= 3
                    if (commonWordsWithVerse >= 3) {
                        // Calculate bonus for lined-up pairs of words with an index difference of 1
                        val relatedWordsList = relatedWordsSet.toList()

                        for (i in 0 until relatedWordsList.size - 1) {
                            for (j in 0 until verseWordsList.size - 1) {
                                if (relatedWordsList[i] == verseWordsList[j] &&
                                    relatedWordsList[i + 1] == verseWordsList[j + 1]) {
                                    bonus += 10 // or any other bonus value you deem appropriate
                                }
                                // Check for an index difference of 2
                                if (i < relatedWordsList.size - 2 && j < verseWordsList.size - 2 &&
                                    relatedWordsList[i] == verseWordsList[j] &&
                                    relatedWordsList[i + 2] == verseWordsList[j + 2]) {
                                    bonus += 5  // or any other bonus value you deem appropriate
                                }
                            }
                        }
                    }

                    totalCommonWords += bonus

                    val denominator = ceil(totalWords.toDouble() / 50.0)
                    val score = totalCommonWords.toDouble() / denominator

                    matchingVerses.add(Quadruple(chapter, verseNumber, verse, score))
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        matchingVerses.sortByDescending { it.fourth }

        val topNVerses = matchingVerses.take(topN)

        return topNVerses.shuffled().take(selectN).map { Triple(it.first, it.second, it.third) }
            .map { Verse(it.first, it.second, it.third) }
    }

    private fun searchVerses(context: Context, prompt: String): List<Verse> {
        val dbName = "bible.db"
        val dbPath = context.getDatabasePath(dbName).absolutePath
        val db: SQLiteDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

        val cursor = db.rawQuery("SELECT bible_book, Verse_Number, Verse FROM Bible WHERE Verse LIKE ?", arrayOf("%$prompt%"))

        val verses = mutableListOf<Verse>()

        if (cursor.moveToFirst()) {
            do {
                val chapter = cursor.getString(0)
                val verseNumber = cursor.getString(1)
                val verse = cursor.getString(2)
                verses.add(Verse(chapter, verseNumber, verse))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return verses
    }

    private fun addToFavorites(context: Context, verses: List<Verse>) {
        val db = AppDatabase.getDatabase(context)
        val favoriteVerseDao = db.favoriteVerseDao()

        GlobalScope.launch(Dispatchers.IO) {
            for (verse in verses) {
                val favoriteVerse = FavoriteVerse(verse.chapter, verse.verseNumber, verse.text, addedAt = System.currentTimeMillis())
                favoriteVerseDao.insert(favoriteVerse)
            }
        }
    }

    data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
