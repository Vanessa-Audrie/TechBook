package com.example.project_pemob_techie.ui.content

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.databinding.ActivitySearchResultBinding
import com.google.firebase.database.*

class SearchResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchResultBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: RecAdapter
    private val searchResults = mutableListOf<BookResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val query = intent.getStringExtra("SEARCH_QUERY") ?: ""
        Log.d("SearchResultActivity", "Search query: $query")
        val textView: TextView = findViewById(R.id.textView60)
        textView.text = "Search Results For \"$query\""

        // Set up RecyclerView
        val recyclerView: RecyclerView = binding.viewResult
        Log.d("RecyclerViewCheck", "RecyclerView initialized: $recyclerView")
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = RecAdapter(searchResults)
        recyclerView.adapter = adapter

        database =
            FirebaseDatabase.getInstance("https://techbook-6099b-default-rtdb.firebaseio.com/")
                .getReference("2/data/")

        performSearch(query)

        val backButton: ImageView = findViewById(R.id.imageView25)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            Toast.makeText(this, "Search query is empty", Toast.LENGTH_SHORT).show()
            return
        }

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                searchResults.clear()
                for (dataSnapshot in snapshot.children) {
                    val book = dataSnapshot.getValue(BookResponse::class.java)
                    if (book != null) {
                        Log.d("FirebaseBook", "Book: ${book.book_title}, Author: ${book.author}")
                    }

                    // Filter by title or author
                    if (book != null && (book.book_title?.contains(query, true) == true ||
                                book.author?.contains(query, true) == true)) {
                        searchResults.add(book)
                    }
                }

                // Log hasil pencarian
                Log.d("SearchResultActivity", "Search results: $searchResults")

                // Update RecyclerView
                adapter.notifyDataSetChanged()

                if (searchResults.isEmpty()) {
                    Toast.makeText(this@SearchResultActivity, "No results found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SearchResultActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
