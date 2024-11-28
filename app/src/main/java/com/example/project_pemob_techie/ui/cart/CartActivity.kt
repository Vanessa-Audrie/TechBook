package com.example.project_pemob_techie.ui.cart

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.content.CartAdapter
import com.google.firebase.database.*

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerViewCart: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartItems: MutableList<CartItem>
    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var database: DatabaseReference
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        userId = getUserIdFromSession()
        if (userId.isEmpty()) {
            finish()
            return
        }

        val backButton: ImageView = findViewById(R.id.imageView3)
        backButton.setOnClickListener {
            finish()
        }
        recyclerViewCart = findViewById(R.id.viewCart)
        recyclerViewCart.layoutManager = LinearLayoutManager(this)

        cartItems = mutableListOf()
        cartAdapter = CartAdapter(this, cartViewModel.cartItems.value ?: mutableListOf())
        recyclerViewCart.adapter = cartAdapter

        cartViewModel.cartItems.observe(this, { items -> cartAdapter.updateCart(items)
            recyclerViewCart.adapter = cartAdapter
        })

        loadCartItems()
    }

    private fun loadCartItems() {
        val cartRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("3/cart/userId/$userId")

        cartRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartItems.clear()
                for (data in snapshot.children) {
                    val cartItem = data.getValue(CartItem::class.java)
                    if (cartItem != null) {
                        cartItems.add(cartItem)
                        Log.d("CartActivity", "Item successfully added: $cartItem")
                    } else {
                        Log.e("CartActivity", "Fail in adding item: ${data.value}")
                    }
                }
                cartAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CartActivity", "Error : ${error.message}")
            }
        })
    }

    private fun getUserIdFromSession(): String {
        return "YOUR_USER_ID"
    }
}