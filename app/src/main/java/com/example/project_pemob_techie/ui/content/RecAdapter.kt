package com.example.project_pemob_techie.ui.content

import android.util.Base64
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R

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
        return ViewHolder(view)
    }

    fun hexStringToByteArray(hexString: String): ByteArray {
        val result = ByteArray(hexString.length / 2)
        for (i in hexString.indices step 2) {
            val byte = hexString.substring(i, i + 2).toInt(16).toByte()
            result[i / 2] = byte
        }
        return result
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
}
