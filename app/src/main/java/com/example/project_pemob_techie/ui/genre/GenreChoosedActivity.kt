package com.example.project_pemob_techie.ui.genre

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.cart.CartActivity
import com.example.project_pemob_techie.ui.content.BookResponse
import com.example.project_pemob_techie.ui.content.RecAdapter
import com.google.firebase.database.*

class GenreChoosedActivity : AppCompatActivity() {

    private lateinit var genre: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var recAdapter: RecAdapter
    private lateinit var booksRef: DatabaseReference
    private val genreResults = mutableListOf<BookResponse>()
    private var lastVisibleBookKey: String? = null
    private var isLoading = false
    private var isLastPage = false
    private lateinit var progressBar: View
    private lateinit var title: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genre_choosed)
        progressBar = findViewById(R.id.progressBar)
        val backButton: ImageView = findViewById(R.id.imageView23)
        val cartButton: ImageView = findViewById(R.id.imageView24)
        var title: TextView = findViewById(R.id.textView59)


        genre = intent.getStringExtra("GENRE")?.lowercase() ?: ""

        title.text = genre.replaceFirstChar { it.uppercase() }

        recyclerView = findViewById(R.id.recyclerViewBooks)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        recAdapter = RecAdapter(genreResults)
        recyclerView.adapter = recAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && !isLastPage && totalItemCount <= (lastVisibleItem + 5)) {
                    loadBooksByGenre()
                }
            }
        })

        loadBooksByGenre()

        Log.d("Selected", "Genre: $genre")

        backButton.setOnClickListener {
            finish()
        }

        cartButton.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)

        }
    }

    private fun loadBooksByGenre() {
        showLoading(true)
        
        booksRef = FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("2/data/")

        var query: Query = booksRef.orderByKey().limitToFirst(70)


        if (!lastVisibleBookKey.isNullOrEmpty()) {
            query = booksRef.orderByKey().startAfter(lastVisibleBookKey).limitToFirst(70)
        }

        isLoading = true

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                showLoading(false)
                if (snapshot.exists() && snapshot.children.count() > 0) {
                    val booksToAdd = mutableListOf<BookResponse>()

                    for (dataSnapshot in snapshot.children) {
                        val book = dataSnapshot.getValue(BookResponse::class.java)
                        if (book != null) {
                            val bookGenre = book.genre?.lowercase()
                            if (bookGenre != null && bookGenre.contains(genre)) {
                                booksToAdd.add(book)
                            }
                        }
                    }

                    if (booksToAdd.isNotEmpty()) {
                        genreResults.addAll(booksToAdd)
                        recAdapter.notifyDataSetChanged()

                        lastVisibleBookKey = snapshot.children.lastOrNull()?.key
                    } else {
                        Toast.makeText(this@GenreChoosedActivity, "No more books available", Toast.LENGTH_SHORT).show()
                        isLastPage = true
                    }
                } else {
                    Toast.makeText(this@GenreChoosedActivity, "No more books available", Toast.LENGTH_SHORT).show()
                    isLastPage = true
                }

                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                Toast.makeText(this@GenreChoosedActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }


}
