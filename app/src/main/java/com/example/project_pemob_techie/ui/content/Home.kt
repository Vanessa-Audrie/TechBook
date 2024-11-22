package com.example.project_pemob_techie.ui.content

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.content.BookResponse
import com.example.project_pemob_techie.ui.content.RecAdapter
import com.google.firebase.database.*


class Home : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var searchBar: EditText
    private val recommendations = mutableListOf<BookResponse>()
    private val filteredList = mutableListOf<BookResponse>()
    private lateinit var adapter: RecAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_home)

        recyclerView = findViewById(R.id.viewRecom)
        searchBar = findViewById(R.id.search_bar)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        adapter = RecAdapter(filteredList)
        recyclerView.adapter = adapter

        val database =
            FirebaseDatabase.getInstance("https://techbook-6099b-default-rtdb.firebaseio.com/")
                .getReference("2/data/")

        database.limitToFirst(10).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recommendations = mutableListOf<BookResponse>()

                for (dataSnapshot in snapshot.children) {
                    val book = dataSnapshot.getValue(BookResponse::class.java)
                    book?.let {
                        recommendations.add(it)
                    }
                }
                val searchBar = findViewById<EditText>(R.id.search_bar)

                searchBar.setOnEditorActionListener { v, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                        val query = v.text.toString().trim()
                        Log.d("Search", "Query entered: $query")

                        if (query.isNotEmpty()) {
                            val intent = Intent(this@Home, SearchResultActivity::class.java)
                            intent.putExtra("SEARCH_QUERY", query)
                            startActivity(intent)
                        }

                        true
                    } else {
                        false
                    }
                }
                updateFilteredList()
                recyclerView.adapter = RecAdapter(recommendations)
            }

            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
            }
        })

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateFilteredList()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateFilteredList() {
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                adapter.updateFilteredList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}


