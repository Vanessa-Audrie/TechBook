package com.example.project_pemob_techie.ui.history

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.databinding.ViewholderInsertRatingBinding

class ReviewAdapter(
    private val items: MutableList<ReviewItem>,
    private val onItemUpdated: (ReviewItem, Int) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(private val binding: ViewholderInsertRatingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReviewItem, position: Int) {
            binding.tvReviewTitle.text = "Review for ${item.bookTitle}"
            binding.ratingBar.rating = item.rating
            binding.etReview.setText(item.review)

            binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                item.rating = rating
                onItemUpdated(item, position)
            }

            binding.etReview.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    item.review = s.toString()
                }
                override fun afterTextChanged(s: Editable?) {
                    onItemUpdated(item, position)
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ViewholderInsertRatingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}

