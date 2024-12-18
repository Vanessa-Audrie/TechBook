package com.example.project_pemob_techie.ui.content

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RatingAdapter(
    private val reviews: List<Review>
) : RecyclerView.Adapter<RatingAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.viewholder_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.bind(review)
    }

    override fun getItemCount(): Int = reviews.size

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar7)
        private val reviewTextView: TextView = itemView.findViewById(R.id.textView52)
        private val nameTextView: TextView = itemView.findViewById(R.id.textView48)
        private val timestampTextView: TextView = itemView.findViewById(R.id.textView68)
        private val profileImageView: ImageView = itemView.findViewById(R.id.imageView39)
        private val database: DatabaseReference = FirebaseDatabase.getInstance(
            "https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).getReference("techbook_techie/user")

        fun bind(review: Review) {
            ratingBar.rating = review.rating.toFloat()
            ratingBar.isEnabled = false

            reviewTextView.text = review.review
            nameTextView.text = review.userName
            timestampTextView.text = review.timestamp

            fetchProfileImageUrl(review.userId)
        }

        private fun fetchProfileImageUrl(userId: String) {
            database.child(userId).child("profileImageUrl")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val base64Image = snapshot.getValue(String::class.java)
                        if (!base64Image.isNullOrEmpty()) {
                            try {
                                val decodedBytes = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT)

                                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                                profileImageView.setImageBitmap(bitmap)
                            } catch (e: IllegalArgumentException) {
                                e.printStackTrace()
                                profileImageView.setImageResource(R.drawable.error)
                            }
                        } else {
                            profileImageView.setImageResource(R.drawable.error)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        profileImageView.setImageResource(R.drawable.error)
                    }
                })
        }


    }



    data class Review(
        val userId: String,
        val rating: Int,
        val review: String,
        val userName: String,
        val timestamp: String
    )
}
