package com.example.project_pemob_techie.ui.genre

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
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
    private val genreCache = mutableMapOf<String, List<BookResponse>>()
    private var isLoading = false
    private var isLastPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genre_choosed)

        val backButton: ImageView = findViewById(R.id.imageView23)
        genre = intent.getStringExtra("GENRE")?.lowercase() ?: ""

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
                    loadBooksByGenre(genre)
                }
            }
        })

        loadBooksByGenre(genre)
        Log.d("Selected", "Genre: $genre")

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadBooksByGenre(genre: String) {
        if (genreCache.containsKey(genre)) {
            genreResults.addAll(genreCache[genre]!!)
            recAdapter.notifyDataSetChanged()
            return
        }

        booksRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("2/data/")

        var query: Query = booksRef.orderByChild("genre").limitToFirst(60)

        if (!lastVisibleBookKey.isNullOrEmpty()) {
            query = query.startAfter(lastVisibleBookKey)
        }

        isLoading = true

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

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
                    genreCache[genre] = genreResults.toList()
                    recAdapter.notifyDataSetChanged()

                    lastVisibleBookKey = snapshot.children.lastOrNull()?.key

                } else {
                    Toast.makeText(this@GenreChoosedActivity, "No books found", Toast.LENGTH_SHORT).show()
                    isLastPage = true
                }

                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GenreChoosedActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        })
    }
}



