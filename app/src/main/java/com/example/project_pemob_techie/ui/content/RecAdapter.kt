package com.example.project_pemob_techie.ui.content

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.content.BookResponse
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class RecAdapter(private val recommendations: List<BookResponse>) :
    RecyclerView.Adapter<RecAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.recomTitle)
        val price: TextView = view.findViewById(R.id.price)
        val image: ImageView = view.findViewById(R.id.imageView)
        val button: Button = view.findViewById(R.id.button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.viewholder_recom_list, parent, false)
        val holder = ViewHolder(view)

        holder.title.setOnClickListener { v ->
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val recommendation = recommendations[position]
                val context = v.context
                val intent = Intent(context, BookDetails::class.java).apply {
                    putExtra("BOOK_TITLE", recommendation.book_title)
                    putExtra("BOOK_PRICE", recommendation.price)
                    val imagePath = recommendation.book_img?.let { saveHexImageToFile(context, it) }
                    putExtra("BOOK_IMAGE", imagePath)  // Pass the image file path here
                    putExtra("BOOK_SYNOPSIS", recommendation.synopsis)
                    putExtra("BOOK_ISBN", recommendation.isbn)
                    putExtra("BOOK_AUTHOR", recommendation.author)
                    putExtra("BOOK_LANG", recommendation.language)
                    putExtra("BOOK_PAGES", recommendation.number_of_pages)
                    putExtra("BOOK_DATE", recommendation.published_date)
                    putExtra("BOOK_MASS", recommendation.mass)
                    putExtra("BOOK_PUBLISHER", recommendation.publisher)
                }
                context.startActivity(intent)
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recommendation = recommendations[position]

        holder.title.text = recommendation.book_title ?: "No Title Available"
        holder.price.text = "Rp ${recommendation.price ?: "0"}"

        val hexImage = recommendation.book_img
        if (!hexImage.isNullOrEmpty()) {
            try {
                val cleanHexImage = if (hexImage.startsWith("0x")) hexImage.substring(2) else hexImage
                val imageBytes = hexStringToByteArray(cleanHexImage)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (bitmap != null) {
                    holder.image.setImageBitmap(bitmap)
                } else {
                    Log.e("RecAdapter", "Bitmap decoding returned null - check the hex data.")
                }
            } catch (e: Exception) {
                Log.e("RecAdapter", "Failed to decode hex image data at position $position", e)
            }
        } else {
            Log.e("RecAdapter", "Empty or null hex image string at position $position")
        }
    }

    override fun getItemCount(): Int = recommendations.size

    private fun saveHexImageToFile(context: Context, hexString: String): String? {
        try {
            val bytes = hexStringToByteArray(hexString)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            if (bitmap != null) {
                val file = File(context.cacheDir, "book_image_${System.currentTimeMillis()}.png")
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                return file.absolutePath
            }
        } catch (e: IOException) {
            Log.e("RecAdapter", "Failed to save image to file", e)
        }
        return null
    }

    private fun hexStringToByteArray(hexString: String): ByteArray {
        // Remove the "0x" prefix if it exists
        val cleanHexString = if (hexString.startsWith("0x")) hexString.substring(2) else hexString

        val result = ByteArray(cleanHexString.length / 2)
        for (i in cleanHexString.indices step 2) {
            try {
                val byte = cleanHexString.substring(i, i + 2).toInt(16).toByte()
                result[i / 2] = byte
            } catch (e: NumberFormatException) {
                Log.e("RecAdapter", "Failed to convert hex string to byte array: ${e.message}")
            }
        }
        return result
    }

}
