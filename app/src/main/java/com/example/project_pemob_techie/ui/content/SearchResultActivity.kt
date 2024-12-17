package com.example.project_pemob_techie.ui.content

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.google.firebase.database.*
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

    private var selectedRatings: Set<Int> = emptySet()

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

        val radioButton1 = popupView.findViewById<RadioButton>(R.id.checkBox8)
        val radioButton2 = popupView.findViewById<RadioButton>(R.id.checkBox9)
        val radioButton3 = popupView.findViewById<RadioButton>(R.id.checkBox10)
        val radioButton4 = popupView.findViewById<RadioButton>(R.id.checkBox11)
        val radioButton5 = popupView.findViewById<RadioButton>(R.id.checkBox12)

        selectedGenre?.let { genreDropdown.setText(it, false) }

        when (selectedPriceRange) {
            0 to 50000 -> priceRadioGroup.check(R.id.RB_price1)
            50000 to 150000 -> priceRadioGroup.check(R.id.RB_price2)
            150000 to 250000 -> priceRadioGroup.check(R.id.RB_price3)
            250000 to 350000 -> priceRadioGroup.check(R.id.RB_price4)
            350000 to 450000 -> priceRadioGroup.check(R.id.RB_price5)
        }

        radioButton1.isChecked = selectedRatings.contains(1)
        radioButton2.isChecked = selectedRatings.contains(2)
        radioButton3.isChecked = selectedRatings.contains(3)
        radioButton4.isChecked = selectedRatings.contains(4)
        radioButton5.isChecked = selectedRatings.contains(5)

        resetButton.setOnClickListener {
            genreDropdown.setText("", false)
            priceRadioGroup.clearCheck()
            radioButton1.isChecked = false
            radioButton2.isChecked = false
            radioButton3.isChecked = false
            radioButton4.isChecked = false
            radioButton5.isChecked = false
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

            selectedRatings = setOfNotNull(
                if (radioButton1.isChecked) 1 else null,
                if (radioButton2.isChecked) 2 else null,
                if (radioButton3.isChecked) 3 else null,
                if (radioButton4.isChecked) 4 else null,
                if (radioButton5.isChecked) 5 else null
            )
            applyFilters()
            popupWindow.dismiss()
        }

        popupWindow.showAtLocation(findViewById(R.id.imageView27), Gravity.CENTER, 0, 0)
    }


    private fun applyFilters() {
        searchResults.clear()
        val filteredBooks = searchCache.values.flatten().filter { book ->
            val matchesPrice = selectedPriceRange?.let { range ->
                val price = book.price?.toIntOrNull()
                price in range.first..range.second
            } ?: true

            val matchesGenre = selectedGenre?.let { genre ->
                book.genre?.split("_")?.any { it.contains(genre, ignoreCase = true) } == true
            } ?: true

            matchesPrice && matchesGenre
        }
        if (selectedRatings.isEmpty()) {
            searchResults.addAll(filteredBooks)
            adapter.notifyDataSetChanged()
            return
        }
        val booksWithRatings = mutableListOf<BookResponse>()
        var remainingRequests = filteredBooks.size
        if (remainingRequests == 0) {
            adapter.notifyDataSetChanged()
            return
        }
        for (book in filteredBooks) {
            book.isbn?.let { isbn ->
                getAverageRating(isbn) { averageRating ->
                    if (selectedRatings.contains(averageRating)) {
                        booksWithRatings.add(book)
                    }
                    remainingRequests--
                    if (remainingRequests == 0) {
                        searchResults.addAll(booksWithRatings)
                        adapter.notifyDataSetChanged()
                    }
                }
            } ?: run {
                remainingRequests--
                if (remainingRequests == 0) {
                    searchResults.addAll(booksWithRatings)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }


    private fun getAverageRating(isbn: String, callback: (Int) -> Unit) {
        var totalRating = 0
        var reviewCount = 0
        val database = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("5/rating_reviews/$isbn")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    for (transactionSnapshot in userSnapshot.children) {
                        val rating = transactionSnapshot.child("rating").getValue(Int::class.java) ?: 0
                        totalRating += rating
                        reviewCount++
                    }
                }
                val averageRating = if (reviewCount > 0) {
                    (totalRating / reviewCount).toFloat().toInt()
                } else {
                    0
                }
                callback(averageRating)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SearchResultActivity, "Failed to fetch rating", Toast.LENGTH_SHORT).show()
                callback(0)
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
