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

        userId = getUserIdFromSession() // Ambil userId dari session atau intent
        if (userId.isEmpty()) {
            finish() // Jika userId kosong, keluar dari activity
            return
        }

        val backButton: ImageView = findViewById(R.id.imageView3)
        backButton.setOnClickListener {
            finish() // Kembali ke activity sebelumnya
        }
        recyclerViewCart = findViewById(R.id.viewCart)
        recyclerViewCart.layoutManager = LinearLayoutManager(this)

        cartItems = mutableListOf()
        cartAdapter = CartAdapter(this, cartViewModel.cartItems.value ?: mutableListOf())
        recyclerViewCart.adapter = cartAdapter

        // Observe changes to the cart items
        cartViewModel.cartItems.observe(this, { items -> cartAdapter.updateCart(items)
            recyclerViewCart.adapter = cartAdapter
        })

        // Load data dari Firebase
        loadCartItems()
    }

    private fun loadCartItems() {
        // Referensi ke Firebase berdasarkan userId
        val cartRef = FirebaseDatabase.getInstance("https://techbook-6099b-default-rtdb.firebaseio.com/")
            .getReference("3/cart/userId/$userId")

        cartRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartItems.clear() // Bersihkan list sebelumnya
                for (data in snapshot.children) {
                    val cartItem = data.getValue(CartItem::class.java)
                    if (cartItem != null) {
                        cartItems.add(cartItem)
                        Log.d("CartActivity", "Item berhasil ditambahkan: $cartItem")
                    } else {
                        Log.e("CartActivity", "Gagal parse item: ${data.value}")
                    }
                }
                cartAdapter.notifyDataSetChanged() // Update RecyclerView
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CartActivity", "Error membaca data: ${error.message}")
            }
        })
    }

    private fun getUserIdFromSession(): String {
        // Ganti dengan logika untuk mengambil userId dari session atau shared preferences
        return "YOUR_USER_ID" // Dummy ID, ganti dengan nilai sebenarnya
    }
}