package com.example.project_pemob_techie.ui.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R

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

        fun bind(review: Review) {
            ratingBar.rating = review.rating.toFloat()
            ratingBar.isEnabled = false

            reviewTextView.text = review.review
            nameTextView.text = review.userName
            timestampTextView.text = review.timestamp
        }
    }

    data class Review(
        val rating: Int,
        val review: String,
        val userName: String,
        val timestamp: String
    )
}
