package com.example.testapp.ui.bible

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.testapp.data.AppDatabase
import com.example.testapp.data.BibleState
import com.example.testapp.databinding.FragmentBibleBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern
import androidx.lifecycle.Observer
import com.example.testapp.ui.bible.CustomTouchListener

class BibleFragment : Fragment() {

    private val bibleViewModel: BibleViewModel by activityViewModels()
    private var _binding: FragmentBibleBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: BiblePagerAdapter
    private lateinit var pageNumberTextView: TextView
    private lateinit var pageJumpInput: EditText
    private lateinit var pageJumpButton: Button
    private lateinit var buttonLeft: ImageButton
    private lateinit var buttonRight: ImageButton
    private var pages: List<String> = emptyList()
    private var currentPageIndex = 0
    private var initialPageSet = false
    private var isPageFromFavorites = false

    private val bibleStateDao by lazy {
        AppDatabase.getDatabase(requireContext()).bibleStateDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBibleBinding.inflate(inflater, container, false)
        applyGlobalFontSize()
        return binding.root
    }

    private fun applyGlobalFontSize() {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val fontSize = sharedPreferences.getFloat("font_size", 20f) // Default to 20sp
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = binding.viewPager
        pageJumpInput = binding.pageJumpInput
        pageJumpButton = binding.pageJumpButton
        buttonLeft = binding.buttonLeft
        buttonRight = binding.buttonRight

        pageJumpButton.setOnClickListener {
            val pageNumber = pageJumpInput.text.toString().toIntOrNull()
            pageNumber?.let {
                val pageIndex = if (it in 1..pages.size) it - 1 else pages.size - 1
                viewPager.setCurrentItem(pageIndex, true)
            }
            hideKeyboard()
        }

        viewPager.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
                pageJumpInput.clearFocus()
            }
            false
        }

        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
                pageJumpInput.clearFocus()
            }
            false
        }

        observePageIndex()

        lifecycleScope.launch {
            loadBibleText()
            updateBibleBook()
        }

        // Set up left and right arrow button click listeners
        buttonLeft.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem -= 1
            }
        }

        buttonRight.setOnClickListener {
            if (viewPager.currentItem < adapter.itemCount - 1) {
                viewPager.currentItem += 1
            }
        }
    }

    private suspend fun loadBibleText() {
        withContext(Dispatchers.IO) {
            try {
                val bibleText = readTextFromAsset("bible.txt")
                pages = splitTextIntoPages(bibleText, 20)
                withContext(Dispatchers.Main) {
                    setupViewPager()

                    if (bibleViewModel.isOpenInBibleNavigation.value != true) {
                        Log.d("BibleFragment", "IN LOAD BIBLE TEXT")
                        loadState()
                    } else if (isPageFromFavorites) {
                        viewPager.setCurrentItem(currentPageIndex, false)
                        pageNumberTextView.text = "Page ${currentPageIndex + 1}"
                        bibleViewModel.isOpenInBibleNavigation.value = false
                        isPageFromFavorites = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupViewPager() {
        Log.d("BibleFragment", "IN SETUP VIEW PAGE")
        adapter = BiblePagerAdapter(pages, requireContext())
        binding.viewPager.adapter = adapter

        binding.viewPager.offscreenPageLimit = 1

        var isInitialSetup = true

        binding.viewPager.apply {
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (isInitialSetup) return
                    Log.d("BibleFragment", "CURRENT PAGE POSITION $position")
                    currentPageIndex = position

                    lifecycleScope.launch {
                        updateBibleBook()
                    }
                }
            })

            getChildAt(0).setOnTouchListener(CustomTouchListener(this))
        }

        binding.viewPager.post {
            isInitialSetup = false
        }
    }

    private suspend fun readTextFromAsset(fileName: String): String {
        return withContext(Dispatchers.IO) {
            val inputStream = requireContext().assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.use { it.readText() }
        }
    }

    private fun splitTextIntoPages(text: String, occurrences: Int): List<String> {
        val pages = mutableListOf<String>()
        val pattern = Pattern.compile("\\d+:\\d+(?=\\s)")
        val matcher = pattern.matcher(text)

        var startIndex = 0
        var count = 0

        while (matcher.find()) {
            count++
            if (count == occurrences + 1) {
                val endIndex = matcher.start()
                pages.add(text.substring(startIndex, endIndex).trim())
                startIndex = endIndex
                count = 1
            }
        }

        if (startIndex < text.length) {
            pages.add(text.substring(startIndex).trim())
        }

        return pages
    }

    private suspend fun loadState() {
        withContext(Dispatchers.IO) {
            val state = bibleStateDao.getState()
            Log.d("BibleFragment", "Current Page before if check: $currentPageIndex")
            if (currentPageIndex == 0 && bibleViewModel.isOpenInBibleNavigation.value != true) {
                currentPageIndex = state?.pageIndex ?: 0
                Log.d("BibleFragment", "Loading from state in IO context: $currentPageIndex")
            }
            withContext(Dispatchers.Main) {
                if (currentPageIndex == 0 && bibleViewModel.isOpenInBibleNavigation.value != true) {
                    currentPageIndex = state?.pageIndex ?: 0
                    Log.d("BibleFragment", "Loading from state in Main context: $currentPageIndex")
                }
                Log.d("BibleFragment", "Setting ViewPager current item to: $currentPageIndex")
                viewPager.setCurrentItem(currentPageIndex, false)
                pageNumberTextView.text = "Page ${currentPageIndex + 1}"
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("BibleFragment", "BibleFragment paused")
        lifecycleScope.launch {
            saveState()
        }
    }

    private suspend fun saveState() {
        withContext(Dispatchers.IO) {
            val state = BibleState(
                pageIndex = viewPager.currentItem,
                scrollPosition = 0
            )
            bibleStateDao.saveState(state)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hideKeyboard() {
        view?.let { v ->
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    private fun observePageIndex() {
        bibleViewModel.pageIndex.observe(viewLifecycleOwner, Observer { pageIndex ->
            pageIndex?.let {
                if (bibleViewModel.isOpenInBibleNavigation.value == true) {
                    Log.d("BibleFragment", "USING Favorites FRAGMENT pageIndex: $it")
                    currentPageIndex = it
                    isPageFromFavorites = true
                    if (::adapter.isInitialized) {
                        Log.d("BibleFragment", "FAVORITES FRAGMENT, ADAPTER INITIALIZED SET PAGE TO: $it")
                        viewPager.setCurrentItem(it, false)
                        bibleViewModel.isOpenInBibleNavigation.value = false
                        isPageFromFavorites = false
                    }
                }
            }
        })
    }

    private suspend fun getBibleBookFromDatabase(page: Int): String? {
        Log.d("BibleFragment", "IN GET BIBLE BOOK FROM DATABASE USING PAGE $page")
        return withContext(Dispatchers.IO) {
            val dbName = "bible.db"
            val dbPath = requireContext().getDatabasePath(dbName).absolutePath
            val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
            var bibleBook: String? = null
            var textBibleBook: String? = null

            val query = "SELECT bible_book FROM Bible WHERE page = ?"
            val cursor = db.rawQuery(query, arrayOf(page.toString()))
            if (cursor.moveToFirst()) {
                bibleBook = cursor.getString(cursor.getColumnIndexOrThrow("bible_book"))
                textBibleBook = "$bibleBook ($page)"
            }
            cursor.close()
            db.close()

            textBibleBook
        }
    }

    private suspend fun updateBibleBook() {
        val bibleBook = getBibleBookFromDatabase(currentPageIndex + 1)
        bibleBook?.let {
            withContext(Dispatchers.Main) {
                binding.bibleBookTextView.text = it
            }
        }
    }
}
