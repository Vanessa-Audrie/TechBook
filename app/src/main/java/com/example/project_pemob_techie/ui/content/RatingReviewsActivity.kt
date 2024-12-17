package com.example.project_pemob_techie.ui.content

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class RatingReviewsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RatingAdapter
    private lateinit var averageTextView: TextView
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating_reviews)

        recyclerView = findViewById(R.id.recyclerView)
        averageTextView = findViewById(R.id.textView77)
        backButton = findViewById(R.id.imageView40)

        recyclerView.layoutManager = LinearLayoutManager(this)

        backButton.setOnClickListener {
            finish()
        }

        val isbn = intent.getStringExtra("BOOK_ISBN_KEY")
        if (isbn != null) {
            fetchRatingsAndReviews(isbn)
        } else {
            Toast.makeText(this, "Invalid book data", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchRatingsAndReviews(isbn: String) {
        val database = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("5/rating_reviews/$isbn")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allReviews = mutableListOf<RatingAdapter.Review>()
                var totalRating = 0
                var totalReviews = 0
                val userNames = mutableMapOf<String, String>()

                for (userSnapshot in snapshot.children) {
                    for (transactionSnapshot in userSnapshot.children) {
                        val rating = transactionSnapshot.child("rating").getValue(Int::class.java) ?: 0
                        val review = transactionSnapshot.child("review").getValue(String::class.java) ?: "No review"
                        val timestamp = transactionSnapshot.child("timestamp").getValue(Long::class.java) ?: 0
                        val userId = userSnapshot.key ?: "Unknown User"

                        totalRating += rating
                        totalReviews++

                        if (!userNames.containsKey(userId)) {
                            FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                .getReference("techbook_techie/user/$userId")
                                .get()
                                .addOnSuccessListener { userSnapshot ->
                                    val userName = userSnapshot.child("name").getValue(String::class.java) ?: "Unknown User"
                                    userNames[userId] = userName

                                    val formattedTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
                                    allReviews.add(RatingAdapter.Review(userId, rating, review, userName, formattedTimestamp))

                                    updateUI(allReviews, totalRating, totalReviews)
                                }
                        }
                    }
                }

                if (totalReviews == 0) {
                    updateUI(emptyList(), totalRating, totalReviews)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RatingReviewsActivity, "Failed to load ratings and reviews", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI(reviews: List<RatingAdapter.Review>, totalRating: Int, totalReviews: Int) {
        adapter = RatingAdapter(reviews)
        recyclerView.adapter = adapter

        val averageRating = if (totalReviews > 0) totalRating.toFloat() / totalReviews else 0f
        averageTextView.text = String.format(Locale.getDefault(), "%.1f/5.0", averageRating)
    }
}
