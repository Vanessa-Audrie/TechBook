package com.example.project_pemob_techie.ui.content

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SearchResultActivity : AppCompatActivity() {

    private lateinit var searchResultRecyclerView: RecyclerView
    private lateinit var searchResultAdapter: RecAdapter
    private var recommendations: MutableList<BookResponse> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)

        // Back Button
        val backButton: ImageView = findViewById(R.id.imageView25)
        backButton.setOnClickListener {
            finish()
        }

        // Ambil query pencarian dan data buku
        val query = intent.getStringExtra("SEARCH_QUERY") ?: ""
        val textView: TextView = findViewById(R.id.textView60)
        textView.text = "Search Results For \"$query\""

        // Set up RecyclerView
        searchResultRecyclerView = findViewById(R.id.viewResult)
        searchResultRecyclerView.layoutManager = LinearLayoutManager(this)
        searchResultAdapter = RecAdapter(recommendations)
        searchResultRecyclerView.adapter = searchResultAdapter

        Log.d("SearchResultActivity", "RecyclerView adapter terpasang")

        // Ambil data dari Firebase
        fetchBooksFromFirebase(query)
    }

    private fun fetchBooksFromFirebase(query: String) {
        val database = FirebaseDatabase.getInstance("https://techbook-6099b-default-rtdb.firebaseio.com/")
            .getReference("2/data/")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recommendations.clear()

                for (bookSnapshot in snapshot.children) {
                    val book = bookSnapshot.getValue(BookResponse::class.java)
                    if (book != null) {
                        // Filter berdasarkan query
                        if (query.isEmpty() || book.book_title?.contains(query, ignoreCase = true) == true) {
                            recommendations.add(book)
                            Log.d("SearchResultActivity", "Buku ditambahkan: ${book.book_title}")
                        } else {
                            Log.d("SearchResultActivity", "Buku tidak cocok: ${book.book_title}")
                        }
                    } else {
                        Log.e("SearchResultActivity", "Gagal parsing buku: ${bookSnapshot.value}")
                    }
                }

                Log.d("SearchResultActivity", "Total buku ditemukan: ${recommendations.size}")
                searchResultAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error: ${error.message}")
            }
        })
    }
}


