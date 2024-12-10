package com.example.project_pemob_techie.ui.content

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.google.firebase.database.*
import com.bumptech.glide.Glide
import com.example.project_pemob_techie.ui.cart.CartActivity
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class SearchResultActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var adapter: RecAdapter
    private val searchResults = mutableListOf<BookResponse>()
    private lateinit var progressBar: View
    var lastVisibleKey: String? = null
    private val searchCache = mutableMapOf<String, List<BookResponse>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search_result)
        val query = intent.getStringExtra("SEARCH_QUERY") ?: ""
        Log.d("SearchResultActivity", "Search query: $query")

        val textView: TextView = findViewById(R.id.textView60)
        textView.text = "Search Results For \"$query\""

        val recyclerView: RecyclerView = findViewById(R.id.viewResult)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = RecAdapter(searchResults)
        recyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("2/data/")

        performSearch(query)

        val backButton: ImageView = findViewById(R.id.imageView25)
        backButton.setOnClickListener {
            finish()
        }

        val cartButton: ImageView = findViewById(R.id.imageView26)
        cartButton.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        val filterButton: ImageView = findViewById(R.id.imageView27)
        filterButton.setOnClickListener {
            showFilterPopup()
        }

        val searchBar: EditText = findViewById(R.id.search_bar2)
        searchBar.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchQuery = v.text.toString()
                textView.text = "Search Results For \"$searchQuery\""
                performSearch(searchQuery)
                true
            } else {
                false
            }
        }
    }

    private fun performSearch(query: String) {
        progressBar = findViewById(R.id.progressBar2)

        showLoading(true)
        if (query.isEmpty()) {
            Toast.makeText(this, "Search query is empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (searchCache.containsKey(query)) {
            searchResults.clear()
            searchResults.addAll(searchCache[query]!!)
            adapter.notifyDataSetChanged()
            return
        }

        val formattedQuery = query.trim()
        var queryRef = database.orderByChild("keyword")
            .startAt(formattedQuery)
            .endAt("$formattedQuery\uf8ff")

        selectedPriceRange?.let { range ->
            queryRef = queryRef.orderByChild("price").startAt(range.first.toDouble()).endAt(range.second.toDouble())
        }

        selectedGenre?.let { genre ->
            queryRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val filteredResults = mutableListOf<BookResponse>()

                    for (dataSnapshot in snapshot.children) {
                        val book = dataSnapshot.getValue(BookResponse::class.java)
                        if (book != null && book.genre?.contains(genre, ignoreCase = true) == true) {
                            filteredResults.add(book)
                        }
                    }

                    searchResults.clear()
                    searchResults.addAll(filteredResults)
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SearchResultActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        queryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                showLoading(false)
                searchResults.clear()

                for (dataSnapshot in snapshot.children) {
                    val book = dataSnapshot.getValue(BookResponse::class.java)
                    if (book != null) {
                        searchResults.add(book)
                    }
                }

                searchCache[formattedQuery] = searchResults.toList()
                adapter.notifyDataSetChanged()
                lastVisibleKey = snapshot.children.lastOrNull()?.key

                if (searchResults.isEmpty()) {
                    Toast.makeText(this@SearchResultActivity, "No results found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SearchResultActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private var selectedPriceRange: Pair<Int, Int>? = null
    private var selectedGenre: String? = null

    private fun showFilterPopup() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.filter_popup, null)
        val resetButton = popupView.findViewById<Button>(R.id.button9)
        val popupWindow = PopupWindow(
            popupView,
            WindowManager.LayoutParams.MATCH_PARENT,
            (Resources.getSystem().displayMetrics.heightPixels * 0.75).toInt(),
            true
        )


        val genreDropdown = popupView.findViewById<MaterialAutoCompleteTextView>(R.id.materialAutoCompleteTextView)
        val applyButton = popupView.findViewById<Button>(R.id.button8)
        val priceRadioGroup = popupView.findViewById<RadioGroup>(R.id.priceRadioGroup)
        genreDropdown.setTypeface(ResourcesCompat.getFont(this, R.font.montserrat_regular))

        val genres = listOf(
            "Fantasy", "Action", "Comic", "Self-improvement", "Mystery", "History",
            "Philosophy", "Biography", "Business", "Comedy", "Fiction", "Romance",
            "Music", "Children", "Psychology", "Horror", "Spirituality", "Art"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genres)
        genreDropdown.setThreshold(0)
        genreDropdown.setAdapter(adapter)
        genreDropdown.setDropDownHeight(500)
        genreDropdown.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT)


        genreDropdown.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                genreDropdown.showDropDown()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(genreDropdown.windowToken, 0)
            }
        }

        if (selectedGenre != null) {
            genreDropdown.setText(selectedGenre, false)
        }

        selectedPriceRange?.let { range ->
            when (range) {
                0 to 50000 -> priceRadioGroup.check(R.id.RB_price1)
                50000 to 150000 -> priceRadioGroup.check(R.id.RB_price2)
                150000 to 250000 -> priceRadioGroup.check(R.id.RB_price3)
                250000 to 350000 -> priceRadioGroup.check(R.id.RB_price4)
                350000 to 450000 -> priceRadioGroup.check(R.id.RB_price5)
            }
        }


        applyButton.setOnClickListener {
            selectedPriceRange = when (priceRadioGroup.checkedRadioButtonId) {
                R.id.RB_price1 -> 0 to 50000
                R.id.RB_price2 -> 50000 to 150000
                R.id.RB_price3 -> 150000 to 250000
                R.id.RB_price4 -> 250000 to 350000
                R.id.RB_price5 -> 350000 to 450000
                else -> null
            }
            selectedGenre = genreDropdown.text.toString().takeIf { it.isNotBlank() }

            if (selectedPriceRange == null && selectedGenre.isNullOrBlank()) {
                Toast.makeText(this, "Please select at least one filter", Toast.LENGTH_SHORT).show()
            } else {
                applyFilters()
                popupWindow.dismiss()
            }
        }
        popupWindow.showAtLocation(findViewById(R.id.imageView27), Gravity.CENTER, 0, 0)

        resetButton.setOnClickListener {
            genreDropdown.setText("", false)
            priceRadioGroup.clearCheck()
            selectedPriceRange = null
            selectedGenre = null

            searchResults.clear()
            searchResults.addAll(searchCache.values.flatten())
            adapter.notifyDataSetChanged()

            popupWindow.dismiss()
            Toast.makeText(this, "Filters Reset", Toast.LENGTH_SHORT).show()
        }

    }

    private fun applyFilters() {
        searchResults.clear()
        val filteredResults = searchCache.values.flatten().filter { book ->
            val matchesPrice = selectedPriceRange?.let { range ->
                val price = book.price?.toIntOrNull()
                price in range.first..range.second
            } ?: true

            val matchesGenre = selectedGenre?.let { genre ->
                book.genre.equals(genre, ignoreCase = true)
            } ?: true

            matchesPrice as Boolean && matchesGenre
        }
        searchResults.addAll(filteredResults)
        adapter.notifyDataSetChanged()
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
