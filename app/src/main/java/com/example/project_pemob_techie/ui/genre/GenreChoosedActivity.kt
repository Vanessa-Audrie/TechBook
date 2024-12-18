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
        title = findViewById(R.id.textView59)

        genre = intent.getStringExtra("GENRE")?.lowercase() ?: ""
        title.text = genre.replaceFirstChar { it.uppercase() }

        recyclerView = findViewById(R.id.recyclerViewBooks)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        recAdapter = RecAdapter(genreResults)
        recyclerView.adapter = recAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!isLoading && !isLastPage) {
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    if (lastVisibleItemPosition >= layoutManager.itemCount - 9) {
                        loadBooksByGenre()
                    }
                }
            }
        })

        loadBooksByGenre()

        Log.d("Selected", "Genre: $genre")

        backButton.setOnClickListener { finish() }
        cartButton.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadBooksByGenre() {
        if (isLoading || isLastPage) return
        if (genreResults.isEmpty()) {
            showLoading(true)
        }
        isLoading = true
        booksRef = FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("2/data/")
        val query: Query = if (lastVisibleBookKey.isNullOrEmpty()) {
            booksRef.orderByKey().limitToFirst(50)
        } else {
            booksRef.orderByKey().startAfter(lastVisibleBookKey).limitToFirst(50)
        }
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (genreResults.isEmpty()) {
                    showLoading(false)
                }
                if (snapshot.exists()) {
                    val booksToAdd = snapshot.children.mapNotNull { it.getValue(BookResponse::class.java) }
                        .filter { it.genre?.lowercase()?.contains(genre) == true }

                    if (booksToAdd.isNotEmpty()) {
                        genreResults.addAll(booksToAdd)
                        recAdapter.notifyDataSetChanged()

                        lastVisibleBookKey = snapshot.children.lastOrNull()?.key
                    } else {
                        isLastPage = true
                        Toast.makeText(this@GenreChoosedActivity, "No more books available", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    isLastPage = true
                    Toast.makeText(this@GenreChoosedActivity, "No more books available", Toast.LENGTH_SHORT).show()
                }

                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                if (genreResults.isEmpty()) {
                    showLoading(false)
                }
                isLoading = false
                Toast.makeText(this@GenreChoosedActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
