package com.example.project_pemob_techie.ui.wishlist

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project_pemob_techie.R
import java.io.ByteArrayInputStream


class WishlistAdapter(private val wishlistItems: List<WishlistItem>) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        // Inflate the item layout
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_wishlist, parent, false)
        return WishlistViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        val (bookId, image, price) = wishlistItems[position]

        // Bind the data to the views
        holder.bookIdTextView.text = bookId
        holder.bookPriceTextView.text = "Rp $price" // Display price

        if (image != null) {
            // Decode Base64 to Bitmap
            val decodedBytes = Base64.decode(image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            holder.bookImageView.setImageBitmap(bitmap)
        } else {
            holder.bookImageView.setImageResource(R.drawable.error)
        }
    }

    override fun getItemCount(): Int = wishlistItems.size

    inner class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookIdTextView: TextView = itemView.findViewById(R.id.textView5)
        val bookImageView: ImageView = itemView.findViewById(R.id.imageView5)
        val bookPriceTextView: TextView = itemView.findViewById(R.id.textView6)
    }
}

data class WishlistItem(
    val bookId: String,
    val image: String?,
    val price: String
)

