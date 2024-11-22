package com.example.project_pemob_techie.ui.wishlist

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import coil.load
import coil.size.Scale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class WishlistAdapter(private val wishlistItems: List<WishlistItem>) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_wishlist, parent, false)
        return WishlistViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        val (bookTitle, image, price) = wishlistItems[position]

        holder.bookTitleTextView.text = bookTitle
        holder.bookPriceTextView.text = "Rp $price"

        if (!image.isNullOrEmpty()) {
            loadImageAsync(image, holder.bookImageView, holder.itemView.context)
        } else {
            holder.bookImageView.setImageResource(R.drawable.error)
        }
    }

    private fun loadImageAsync(imageString: String, imageView: ImageView, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val imageBytes = decodeImage(imageString)
            if (imageBytes.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                val filePath = saveImageToFile(context, imageBytes, "book_image_${System.currentTimeMillis()}")
                withContext(Dispatchers.Main) {
                    if (bitmap != null) {
                        imageView.load(bitmap) {
                            crossfade(true)
                            error(R.drawable.error)
                            scale(Scale.FIT)
                        }
                    } else {
                        imageView.setImageResource(R.drawable.error)
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    imageView.setImageResource(R.drawable.error)
                }
            }
        }
    }

    private fun decodeImage(imageString: String): ByteArray {
        return if (imageString.startsWith("0x")) {
            hexStringToByteArray(imageString.removePrefix("0x"))
        } else {
            cleanBase64String(imageString)?.let {
                Base64.decode(it, Base64.DEFAULT)
            } ?: byteArrayOf()
        }
    }

    private fun saveImageToFile(context: Context, imageBytes: ByteArray, fileName: String): String {
        val file = File(context.cacheDir, "$fileName.png")
        try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(imageBytes)
                outputStream.flush()
            }
        } catch (e: IOException) {
            Log.e("WishlistAdapter", "Error saving image to file: ${e.message}")
        }
        return file.absolutePath
    }

    private fun cleanBase64String(base64String: String?): String? {
        return if (!base64String.isNullOrEmpty() && base64String.startsWith("data:image")) {
            base64String.substringAfter(",")
        } else {
            base64String
        }
    }

    private fun hexStringToByteArray(hexString: String): ByteArray {
        return ByteArray(hexString.length / 2) { i ->
            hexString.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }

    override fun getItemCount(): Int = wishlistItems.size

    inner class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookTitleTextView: TextView = itemView.findViewById(R.id.textView5)
        val bookImageView: ImageView = itemView.findViewById(R.id.imageView5)
        val bookPriceTextView: TextView = itemView.findViewById(R.id.textView6)
    }
}

data class WishlistItem(
    val bookTitle: String,
    val image: String?,
    val price: String
)
