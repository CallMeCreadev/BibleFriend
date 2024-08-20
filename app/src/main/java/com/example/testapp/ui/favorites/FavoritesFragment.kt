package com.example.testapp.ui.favorites

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testapp.R
import com.example.testapp.data.AppDatabase
import com.example.testapp.data.FavoriteVerse
import com.example.testapp.databinding.FragmentFavoritesBinding
import com.example.testapp.ui.bible.BibleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.navigation.fragment.findNavController

class FavoritesFragment : Fragment() {

    private val bibleViewModel: BibleViewModel by activityViewModels()
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private var selectedVerses: MutableList<FavoriteVerse> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.favoritesRecyclerView
        val openInBibleButton = binding.openInBibleButton.apply {
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        }
        val removeFromFavoritesButton = binding.removeFromFavoritesButton

        recyclerView.layoutManager = LinearLayoutManager(context)

        favoritesAdapter = FavoritesAdapter(emptyList(), ::onVerseChecked, requireContext())
        recyclerView.adapter = favoritesAdapter

        openInBibleButton.setOnClickListener {
            val selectedVerse = selectedVerses.firstOrNull()
            if (selectedVerse != null) {
                openInBible(selectedVerse)
            }
        }

        removeFromFavoritesButton.setOnClickListener {
            confirmRemoveFromFavorites(selectedVerses)
        }

        loadFavorites()
    }

    private fun onVerseChecked(verse: FavoriteVerse, isChecked: Boolean) {
        if (isChecked) {
            selectedVerses.add(verse)
        } else {
            selectedVerses.remove(verse)
        }
        updateButtons()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateButtons() {
        val openInBibleButton = binding.openInBibleButton
        val removeFromFavoritesButton = binding.removeFromFavoritesButton

        if (selectedVerses.isNotEmpty()) {
            openInBibleButton.visibility = View.VISIBLE
            removeFromFavoritesButton.visibility = View.VISIBLE

            if (selectedVerses.size > 1) {
                openInBibleButton.isEnabled = false
                openInBibleButton.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                openInBibleButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black)) // Ensure text color for disabled state
            } else {
                openInBibleButton.isEnabled = true
                openInBibleButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_color))
                openInBibleButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white)) // Ensure text color for enabled state
            }

            removeFromFavoritesButton.isEnabled = true
            removeFromFavoritesButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_color))
            removeFromFavoritesButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white)) // Ensure text color for enabled state
        } else {
            openInBibleButton.visibility = View.GONE
            removeFromFavoritesButton.visibility = View.GONE
        }
    }

    private fun loadFavorites() {
        val context = requireContext()
        val db = AppDatabase.getDatabase(context)
        val favoriteVerseDao = db.favoriteVerseDao()

        GlobalScope.launch(Dispatchers.IO) {
            val favoriteVerses = favoriteVerseDao.getAllFavorites()
            withContext(Dispatchers.Main) {
                favoritesAdapter.updateData(favoriteVerses)
                updateButtons()  // Ensure buttons are updated after loading data
            }
        }
    }

    private fun openInBible(verse: FavoriteVerse) {
        lifecycleScope.launch {
            val pageIndex = getPageFromDatabase(verse)
            pageIndex?.let {
                // Set the flag before navigating
                bibleViewModel.isOpenInBibleNavigation.value = true
                bibleViewModel.pageIndex.value = it
                Log.d("FavoritesFragment", "Navigating to BibleFragment with pageIndex: $it")
                findNavController().navigate(R.id.navigation_bible)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("FavoritesFragment", "FavoritesFragment resumed")
    }

    override fun onPause() {
        super.onPause()
        Log.d("FavoritesFragment", "FavoritesFragment paused")
    }

    private suspend fun getPageFromDatabase(verse: FavoriteVerse): Int? {
        return withContext(Dispatchers.IO) {
            val dbName = "bible.db"
            val dbPath = requireContext().getDatabasePath(dbName).absolutePath
            val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
            var pageIndex: Int? = null

            val query = "SELECT page FROM Bible WHERE Verse = ? AND Verse_Number = ?"
            val cursor = db.rawQuery(query, arrayOf(verse.text, verse.verse))
            if (cursor.moveToFirst()) {
                pageIndex = cursor.getInt(cursor.getColumnIndexOrThrow("page")) - 1
            }
            cursor.close()
            db.close()

            pageIndex
        }
    }

    private fun confirmRemoveFromFavorites(verses: List<FavoriteVerse>) {
        val context = requireContext()
        AlertDialog.Builder(context)
            .setMessage("Are you sure you want to remove the selected verse(s) from favorites?")
            .setPositiveButton("Yes") { _, _ -> removeFromFavorites(verses) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun removeFromFavorites(verses: List<FavoriteVerse>) {
        val context = requireContext()
        val db = AppDatabase.getDatabase(context)
        val favoriteVerseDao = db.favoriteVerseDao()

        GlobalScope.launch(Dispatchers.IO) {
            for (verse in verses) {
                favoriteVerseDao.delete(verse.chapter, verse.verse)
            }
            withContext(Dispatchers.Main) {
                selectedVerses.clear()  // Clear the selection after removal
                loadFavorites()  // Reload favorites and update buttons
            }
        }
    }
}
