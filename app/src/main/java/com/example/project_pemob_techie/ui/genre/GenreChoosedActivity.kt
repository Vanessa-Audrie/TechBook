package com.example.project_pemob_techie.ui.genre

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.content.BookResponse
import com.example.project_pemob_techie.ui.content.RecAdapter
import com.google.firebase.database.*

class GenreChoosedActivity : AppCompatActivity() {

    private lateinit var genre: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var recAdapter: RecAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genre_choosed)

        genre = intent.getStringExtra("GENRE") ?: ""

        recyclerView = findViewById(R.id.recyclerViewBooks)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inisialisasi adapter dengan daftar kosong
        recAdapter = RecAdapter(emptyList())
        recyclerView.adapter = recAdapter

        Log.d("GenreChoosedActivity", "Selected genre: $genre")

        loadBooksByGenre(genre)
    }

    private fun loadBooksByGenre(genre: String) {
        val booksRef = FirebaseDatabase.getInstance().getReference("books")
        booksRef.orderByChild("genre").equalTo(genre) //menyaring buku sesuai dengan genrenya
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val books = mutableListOf<BookResponse>()
                    for (data in snapshot.children) {
                        val book = data.getValue(BookResponse::class.java)
                        if (book != null) {
                            books.add(book)
                        }
                    }

                    // Update adapter with books based on the selected genre
                    //recAdapter.updateBooks(books)

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@GenreChoosedActivity, "Failed to load books", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
