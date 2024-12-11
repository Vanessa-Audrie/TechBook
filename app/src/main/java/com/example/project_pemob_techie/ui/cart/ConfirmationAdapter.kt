package com.example.project_pemob_techie.ui.cart


import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R

class ConfirmationAdapter(
    private val context: Context,
    var selectedItems: MutableList<CartItem>
) : RecyclerView.Adapter<ConfirmationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookTitle: TextView = view.findViewById(R.id.textView64)
        val bookPrice: TextView = view.findViewById(R.id.textView66)
        val quantity: TextView = view.findViewById(R.id.textView67)
        val bookImage: ImageView = view.findViewById(R.id.imageView33)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.viewholder_checkout_confirmation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartItem = selectedItems[position]
        holder.bookTitle.text = cartItem.title
        holder.bookPrice.text = "Rp ${cartItem.price}"
        holder.quantity.text = "Quantity: ${cartItem.quantity}"

        if (!cartItem.image.isNullOrEmpty()) {
            val imageBytes = hexStringToByteArray(cartItem.image!!)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.bookImage.setImageBitmap(bitmap)
        }
    }

    override fun getItemCount(): Int {
        return selectedItems.size
    }

    private fun hexStringToByteArray(hex: String): ByteArray {
        val result = ByteArray(hex.length / 2)
        for (i in hex.indices step 2) {
            val byte = hex.substring(i, i + 2).toInt(16).toByte()
            result[i / 2] = byte
        }
        return result
    }

}
