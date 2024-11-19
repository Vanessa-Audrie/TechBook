package com.example.project_pemob_techie.ui.content

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.content.BookResponse
import com.example.project_pemob_techie.ui.content.ImageCacheHelper
import com.example.project_pemob_techie.ui.content.RecAdapter
import com.google.firebase.database.*

class Home : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_home)

        recyclerView = findViewById(R.id.viewRecom)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        val database = FirebaseDatabase.getInstance("https://techbook-6099b-default-rtdb.firebaseio.com/")
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

                recyclerView.adapter = RecAdapter(recommendations)
            }

            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
            }
        })

    }
}
