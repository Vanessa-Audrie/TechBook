package com.example.project_pemob_techie.ui.content

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.project_pemob_techie.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class Home : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_home)

        val recyclerView = findViewById<RecyclerView>(R.id.viewRecom)

        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val books = RetrofitInstance.api.getBooks()
                val recommendations = books.map {
                    BookResponse(it.book_title, it.price, it.image_base64)
                }

                launch(Dispatchers.Main) {
                    recyclerView.adapter = RecAdapter(recommendations)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
