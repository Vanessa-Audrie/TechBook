package com.example.project_pemob_techie.ui.history

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_pemob_techie.databinding.ActivityInsertRatingBinding
import com.google.firebase.database.*
import java.util.*

data class ReviewItem(
    val isbn: String,
    val bookTitle: String,
    var rating: Float = 0f,
    var review: String = ""
)

class RatingReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInsertRatingBinding
    private var transactionId: String? = null
    private var userId: String? = null
    private val reviewItems = mutableListOf<ReviewItem>()
    private lateinit var adapter: ReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsertRatingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionId = intent.getStringExtra("TRANSACTION_ID")
        userId = intent.getStringExtra("USER_ID")

        if (transactionId == null || userId == null) {
            Toast.makeText(this, "Invalid transaction or user", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.imageView3.setOnClickListener {
            onBackPressed()
        }

        setupRecyclerView()
        fetchTransactionDetails()
    }

    private fun setupRecyclerView() {
        adapter = ReviewAdapter(reviewItems) { updatedItem, position ->
            reviewItems[position] = updatedItem
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.btnReview2.setOnClickListener {
            if (validateReviews()) {
                submitReviews()
                finish()
            }
        }
    }

    private fun fetchTransactionDetails() {
        val databaseRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("7/transaction_details/$transactionId")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reviewItems.clear()
                for (itemSnapshot in snapshot.children) {
                    val isbn = itemSnapshot.key ?: continue
                    val bookTitle = itemSnapshot.child("title").getValue(String::class.java) ?: "Unknown"

                    reviewItems.add(ReviewItem(isbn, bookTitle))
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RatingReviewActivity, "Failed to fetch transaction details", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun validateReviews(): Boolean {
        for (item in reviewItems) {
            if (item.rating == 0f || item.review.isEmpty()) {
                Toast.makeText(this, "Please provide a rating and review for all items", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun submitReviews() {
        val databaseRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("5/rating_reviews")

        val timestamp = System.currentTimeMillis()
        val tasks = reviewItems.map { item ->
            val ratingData = mapOf(
                "rating" to item.rating,
                "review" to item.review,
                "timestamp" to timestamp
            )
            databaseRef.child(item.isbn).child(userId!!).child(transactionId!!).setValue(ratingData)
        }

        tasks.forEachIndexed { index, task ->
            task.addOnCompleteListener {
                if (it.isSuccessful && index == tasks.size - 1) {
                    Toast.makeText(this, "Reviews submitted successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else if (!it.isSuccessful) {
                    Toast.makeText(this, "Failed to submit reviews. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
