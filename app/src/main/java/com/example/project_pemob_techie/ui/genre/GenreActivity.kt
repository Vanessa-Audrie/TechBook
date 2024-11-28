package com.example.project_pemob_techie.ui.genre

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.databinding.ActivityGenreBinding

class GenreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGenreBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GenreAdapter


    private val genres = listOf(
        "Fantasy", "Action", "Comics", "Self Improvement", "Mystery", "History",
        "Philosophy", "Biography", "Business",  "Manga", "Fiction", "Romance",
        "Music", "Children's", "Psychology", "Horror", "Spirituality", "Art"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recyclerViewGenre
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        adapter = GenreAdapter(genres, ::onGenreClick)
        recyclerView.adapter = adapter
    }

    private fun onGenreClick(genre: String){
        val intent = Intent(this, GenreChoosedActivity::class.java)
        intent.putExtra("GENRE", genre)
        startActivity(intent)
    }
}
