package com.example.project_pemob_techie.ui.content

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.util.Base64

class Home : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_home)

        val recyclerView = findViewById<RecyclerView>(R.id.viewRecom)
        recyclerView.layoutManager = LinearLayoutManager(this)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val books = RetrofitInstance.api.getBooks()
                val recommendations = books.map {
                    DisplayData(it.title, it.price, it.image)
                }

                recyclerView.adapter = RecAdapter(recommendations)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

}
