package com.example.project_pemob_techie.ui.history

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.databinding.ViewholderCheckoutConfirmationBinding
import java.text.NumberFormat
import java.util.Locale

class ItemsAdapter(private val items: List<TransactionItem>) :
    RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ViewholderCheckoutConfirmationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ItemViewHolder(private val binding: ViewholderCheckoutConfirmationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TransactionItem) {
            binding.textView64.text = item.itemName
            val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
            formatter.maximumFractionDigits = 2

            binding.textView67.text = "Quantity: ${item.quantity}"

            val formattedPrice = formatter.format(item.price.toDoubleOrNull() ?: 0.0)
            binding.textView66.text = "Price: Rp $formattedPrice"


            if (item.imageHex.isNotEmpty()) {
                val byteArray = hexStringToByteArray(item.imageHex)
                val bitmap = byteArrayToBitmap(byteArray)
                if (bitmap != null) {
                    binding.imageView33.setImageBitmap(bitmap)
                } else {
                    binding.imageView33.setImageResource(R.drawable.error)
                }
            } else {
                binding.imageView33.setImageResource(R.drawable.error)
            }
        }

        fun hexStringToByteArray(hexString: String): ByteArray {
            val result = ByteArray(hexString.length / 2)
            for (i in hexString.indices step 2) {
                result[i / 2] = hexString.substring(i, i + 2).toInt(16).toByte()
            }
            return result
        }


        private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap? {
            return try {
                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            } catch (e: Exception) {
                null
            }
        }

    }


}
