package com.example.project_pemob_techie.ui.content

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.account.SessionManager
import com.example.project_pemob_techie.ui.cart.CartItem
import com.example.project_pemob_techie.ui.cart.SQLiteHelper
import com.google.firebase.database.FirebaseDatabase
import com.mysql.jdbc.Messages.getString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val context: Context,
    private var cartItems: MutableList<CartItem>,
    private val textView12: TextView,
    private val textView13: TextView,
    private val checkbox2: CheckBox
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private val selectedItems = mutableListOf<String>()
    private lateinit var userId: String
    private val dbHelper = SQLiteHelper(context)
    private var totalQuantity = 0
    private var totalPrice = 0.0

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val itemsFromDb = dbHelper.getAllSelectedItems()
            withContext(Dispatchers.Main) {
                selectedItems.addAll(itemsFromDb)
                notifyDataSetChangedSafely()
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookTitle: TextView = view.findViewById(R.id.textView20)
        val bookPrice: TextView = view.findViewById(R.id.textView21)
        val quantity: TextView = view.findViewById(R.id.textView23)
        val btnIncrease: TextView = view.findViewById(R.id.textView22)
        val btnDecrease: TextView = view.findViewById(R.id.textView24)
        val btnDelete: ImageView = view.findViewById(R.id.imageView10)
        val checkbox: CheckBox = view.findViewById(R.id.checkBox4)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.viewholder_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartItem = cartItems[position]

        holder.bookTitle.text = cartItem.title
        holder.bookPrice.text = "Rp ${cartItem.price}"
        holder.quantity.text = cartItem.quantity.toString()

        if (!cartItem.image.isNullOrEmpty()) {
            val imageBytes = hexStringToByteArray(cartItem.image!!)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.itemView.findViewById<ImageView>(R.id.imageView9).setImageBitmap(bitmap)
        }

        userId = SessionManager.getUserId(context) ?: run {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        holder.btnIncrease.setOnClickListener {
            cartItem.quantity += 1
            holder.quantity.text = cartItem.quantity.toString()

            updateTotal(cartItem, true)

            updateCartInDatabase(cartItem)
            notifyItemChangedSafely(position)
        }

        holder.btnDecrease.setOnClickListener {
            if (cartItem.quantity > 1) {
                cartItem.quantity -= 1
                holder.quantity.text = cartItem.quantity.toString()

                updateTotal(cartItem, false)

                updateCartInDatabase(cartItem)
                notifyItemChangedSafely(position)
            } else {
                Toast.makeText(context, "Quantity cannot be less than 1", Toast.LENGTH_SHORT).show()
            }
        }

        holder.btnDelete.setOnClickListener {
            removeItemFromCart(cartItem, position)
        }

        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = selectedItems.contains(cartItem.isbn)
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            val price = cartItem.price?.toDoubleOrNull() ?: 0.0

            if (isChecked) {
                dbHelper.addSelectedItem(cartItem.isbn)
                selectedItems.add(cartItem.isbn)
                totalQuantity += cartItem.quantity
                totalPrice += price * cartItem.quantity
            } else {
                dbHelper.removeSelectedItem(cartItem.isbn)
                selectedItems.remove(cartItem.isbn)
                totalQuantity -= cartItem.quantity
                totalPrice -= price * cartItem.quantity
            }

            textView12.text = "$totalQuantity"
            val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
            textView13.text = "Rp ${formatter.format(totalPrice)}"

            updateSelectAllCheckboxState()
        }

    }

    fun resetSelectedItems() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    private fun updateTotal(cartItem: CartItem, isIncrease: Boolean) {
        val price = cartItem.price?.toDoubleOrNull() ?: 0.0
        if (isIncrease) {
            totalQuantity += 1
            totalPrice += price
        } else {
            totalQuantity -= 1
            totalPrice -= price
        }

        textView12.text = "$totalQuantity"
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        textView13.text = "Rp ${formatter.format(totalPrice)}"
    }

    private fun updateSelectAllCheckboxState() {
        checkbox2.isChecked = selectedItems.size == cartItems.size
    }

    private fun removeItemFromCart(cartItem: CartItem, position: Int) {
        if (cartItems.isNotEmpty() && position in cartItems.indices) {
            val cartRef = FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("3/cart/userId/$userId/${cartItem.isbn}")
            val tempPosition = position
            cartRef.removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (cartItems.size > tempPosition) {
                            cartItems.removeAt(tempPosition)
                            notifyItemRemoved(tempPosition)
                            Toast.makeText(context, "${cartItem.title} removed from cart", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("CartAdapter", "Position out of bounds after Firebase update")
                        }
                    } else {
                        Toast.makeText(context, "Failed to remove item: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Log.e("CartAdapter", "Invalid position or empty cartItems list: position = $position, cartItems.size = ${cartItems.size}")
        }
    }


    private fun updateCartInDatabase(cartItem: CartItem) {
        val cartRef = FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("3/cart/userId/$userId/${cartItem.isbn}")

        cartRef.setValue(cartItem)
            .addOnSuccessListener {
                Log.d("CartAdapter", "CartItem updated successfully in Firebase")
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update quantity: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun notifyDataSetChangedSafely() {
        CoroutineScope(Dispatchers.Main).launch {
            notifyDataSetChanged()
        }
    }

    private fun notifyItemChangedSafely(position: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            notifyItemChanged(position)
        }
    }

    private fun hexStringToByteArray(hex: String): ByteArray {
        val result = ByteArray(hex.length / 2)
        for (i in hex.indices step 2) {
            result[i / 2] = hex.substring(i, i + 2).toInt(16).toByte()
        }
        return result
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateCart(newItems: List<CartItem>) {
        cartItems = newItems.toMutableList()
        notifyDataSetChangedSafely()
    }

    fun selectAllItems(selectAll: Boolean) {
        totalQuantity = 0
        totalPrice = 0.0
        if (selectAll) {
            // Select all items
            selectedItems.clear()  // Ensure it's cleared before adding all items
            for (cartItem in cartItems) {
                selectedItems.add(cartItem.isbn)
                dbHelper.addSelectedItem(cartItem.isbn)
                totalQuantity += cartItem.quantity
                totalPrice += (cartItem.price?.toDoubleOrNull() ?: 0.0) * cartItem.quantity
            }
        } else {
            dbHelper.clearAllSelectedItems()
            selectedItems.clear()
        }

        textView12.text = "$totalQuantity"
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        textView13.text = "Rp ${formatter.format(totalPrice)}"

        notifyDataSetChangedSafely()
        updateSelectAllCheckboxState() // Update the "select all" checkbox state.
    }


}

