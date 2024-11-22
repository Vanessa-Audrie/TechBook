package com.example.project_pemob_techie.ui.wishlist


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.project_pemob_techie.R
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WishlistAdapter(
    private val wishlistItems: List<WishlistItem>,
    private val onItemRemove: (WishlistItem) -> Unit
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_wishlist, parent, false)
        return WishlistViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        val item = wishlistItems[position]
        val (bookTitle, image, price) = item
        holder.bookTitleTextView.text = bookTitle
        holder.bookPriceTextView.text = "Rp $price"
        if (!image.isNullOrEmpty()) {
            val imageBytes = hexStringToByteArray(image)
            if (imageBytes.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (bitmap != null) {
                    holder.bookImageView.load(bitmap) { }
                } else {
                    holder.bookImageView.setImageResource(R.drawable.error)
                }
            } else {
                holder.bookImageView.setImageResource(R.drawable.error)
            }
        } else {
            holder.bookImageView.setImageResource(R.drawable.error)
        }
        holder.removeButton.setOnClickListener {
            onItemRemove(item)
        }
    }

    override fun getItemCount(): Int = wishlistItems.size

    private fun hexStringToByteArray(hex: String): ByteArray {
        return try {
            val len = hex.length
            if (len % 2 != 0) throw IllegalArgumentException("Invalid hex string length")
            ByteArray(len / 2).apply {
                for (i in 0 until len step 2) {
                    this[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
                }
            }
        } catch (e: Exception) {
            Log.e("WishlistAdapter", "Error decoding hex string: ${e.message}", e)
            byteArrayOf()
        }
    }

    inner class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookTitleTextView: TextView = itemView.findViewById(R.id.textView5)
        val bookImageView: ImageView = itemView.findViewById(R.id.imageView5)
        val bookPriceTextView: TextView = itemView.findViewById(R.id.textView6)
        val removeButton: ImageButton = itemView.findViewById(R.id.imageButton)
    }
}

data class WishlistItem(
    val bookTitle: String,
    val image: String?,
    val price: String
)
