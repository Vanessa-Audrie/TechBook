package com.example.project_pemob_techie.ui.content

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.cart.CartItem
import com.google.firebase.database.FirebaseDatabase

class CartAdapter(
    private val context: Context,
    private var cartItems: MutableList<CartItem>
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookTitle: TextView = view.findViewById(R.id.textView20)
        val bookPrice: TextView = view.findViewById(R.id.textView21)
        val quantity: TextView = view.findViewById(R.id.textView23)
        val btnIncrease: TextView = view.findViewById(R.id.textView22)
        val btnDecrease: TextView = view.findViewById(R.id.textView24)
        val btnDelete: ImageView = view.findViewById(R.id.imageView10)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.viewholder_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartItem = cartItems[position]
        holder.bookTitle.text = cartItem.book_title
        holder.bookPrice.text = "Rp ${cartItem.price}"
        holder.quantity.text = cartItem.quantity.toString()

        if (!cartItem.image.isNullOrEmpty()) {
            val imageBytes = hexStringToByteArray(cartItem.image!!)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.itemView.findViewById<ImageView>(R.id.imageView9).setImageBitmap(bitmap)
        }

        holder.btnIncrease.setOnClickListener {
            cartItem.quantity += 1
            notifyItemChanged(position)
            updateCartInDatabase(cartItem)
        }

        holder.btnDecrease.setOnClickListener {
            if (cartItem.quantity > 1) {
                cartItem.quantity -= 1
                notifyItemChanged(position)
                updateCartInDatabase(cartItem)
            } else {
                Toast.makeText(context, "Quantity cannot be less than 1", Toast.LENGTH_SHORT).show()
            }
        }

        holder.btnDelete.setOnClickListener {
            removeItemFromCart(cartItem, position)
        }
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    fun updateCart(newItems: List<CartItem>) {
        cartItems = newItems.toMutableList()
        notifyDataSetChanged()
    }

    private fun removeItemFromCart(cartItem: CartItem, position: Int) {
        val cartRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("3/cart/userId/${cartItem.bookId}")

        cartRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "${cartItem.book_title} removed from cart", Toast.LENGTH_SHORT).show()
                cartItems.removeAt(position)
                notifyItemRemoved(position)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to remove item: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateCartInDatabase(cartItem: CartItem) {
        val cartRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("3/cart/userId/${cartItem.bookId}")

        cartRef.setValue(cartItem)
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update quantity: ${it.message}", Toast.LENGTH_SHORT).show()
            }
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
