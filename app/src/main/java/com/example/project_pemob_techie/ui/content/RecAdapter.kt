package com.example.project_pemob_techie.ui.content

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.ETC1.decodeImage
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import coil.size.Scale
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.content.BookResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class RecAdapter(private val recommendations: List<BookResponse>) :
    RecyclerView.Adapter<RecAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.recomTitle)
        val price: TextView = view.findViewById(R.id.price)
        val image: ImageView = view.findViewById(R.id.imageView)
    }

    private var filteredRecommendations = mutableListOf<BookResponse>()

    fun updateFilteredList(query: String) {
        filteredRecommendations.clear()
        if (query.isEmpty()) {
            filteredRecommendations.addAll(recommendations)
        } else {
            filteredRecommendations.addAll(recommendations.filter {
                it.book_title?.lowercase()?.contains(query.lowercase()) == true
            })
        }
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.viewholder_recom_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recommendation = recommendations[position]



        holder.title.text = recommendation.book_title ?: "No Title Available"
        holder.price.text = "Rp ${recommendation.price ?: "0"}"



        val imageString = recommendation.book_img
        if (!imageString.isNullOrEmpty()) {
            loadImageAsync(imageString, holder.image, holder.itemView.context)
        } else {
            holder.image.setImageResource(R.drawable.error)
        }

        holder.title.setOnClickListener { v ->
            val context = v.context
            val intent = Intent(context, BookDetails::class.java).apply {
                putExtra("BOOK_TITLE", recommendation.book_title)
                putExtra("BOOK_PRICE", recommendation.price)
                putExtra("BOOK_IMG_PATH", recommendation.book_img?.let { imageString ->
                    saveImageToFile(context, decodeImage(imageString), "book_image_${System.currentTimeMillis()}")
                })
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

    override fun getItemCount(): Int = recommendations.size

    private fun loadImageAsync(imageString: String, imageView: ImageView, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val imageBytes = if (imageString.startsWith("0x")) {
                hexStringToByteArray(imageString.removePrefix("0x"))
            } else {
                cleanBase64String(imageString)?.let {
                    Base64.decode(it, Base64.DEFAULT)
                } ?: byteArrayOf()
            }

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
            Log.e("RecAdapter", "Error saving image to file: ${e.message}")
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
}
