package com.example.project_pemob_techie.ui.content

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.google.firebase.database.*
import com.bumptech.glide.Glide

class SearchResultActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var adapter: RecAdapter
    private val searchResults = mutableListOf<BookResponse>()
    private lateinit var progressBar: View
    var lastVisibleKey: String? = null
    val recyclerView: RecyclerView = findViewById(R.id.viewResult)
    private val searchCache = mutableMapOf<String, List<BookResponse>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)

        val query = intent.getStringExtra("SEARCH_QUERY") ?: ""
        Log.d("SearchResultActivity", "Search query: $query")

        val textView: TextView = findViewById(R.id.textView60)
        textView.text = "Search Results For \"$query\""


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
        progressBar = findViewById(R.id.progressBar)
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
        val queryRef = database.orderByChild("keyword")
            .startAt(formattedQuery)
            .endAt("$formattedQuery\uf8ff")

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

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
}
