package com.example.project_pemob_techie.ui.cart


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.account.SessionManager
import com.example.project_pemob_techie.ui.content.CartAdapter
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.coroutines.launch

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerViewCart: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartItems: MutableList<CartItem>
    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var userId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        userId = getUserIdFromSession() ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
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
        cartAdapter = CartAdapter(this, cartItems)

        progressBar = findViewById(R.id.progressBar2)
        recyclerViewCart.adapter = cartAdapter

        cartViewModel.cartItems.observe(this, { items ->
            cartAdapter.updateCart(items)
        })

        loadCartItems()


        val checkoutButton: Button = findViewById(R.id.button3)
        checkoutButton.setOnClickListener {
            val selectedItems = cartAdapter.getSelectedItemISBNs()

            if (selectedItems.isNotEmpty()) {
                saveSelectedItemsLocally(selectedItems)
                val intent = Intent(this, CheckoutActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun saveSelectedItemsLocally(items: List<String>) {
        val sharedPreferences = getSharedPreferences("cartPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(items)
        editor.putString("selectedItems", json)
        editor.apply()
    }

    private fun loadCartItems() {
        val cartRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("3/cart/userId/$userId/")

        showLoading(true)

        cartRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cartItems = mutableListOf<CartItem>()
                for (itemSnapshot in snapshot.children) {
                    val bookTitle = itemSnapshot.child("title").value as? String
                    val price = itemSnapshot.child("price").value as? String
                    val image = itemSnapshot.child("image").value as? String
                    val quantity = itemSnapshot.child("quantity").value as? Int ?: 1

                    if (bookTitle != null && price != null && image != null) {
                        cartItems.add(CartItem(itemSnapshot.key ?: "", bookTitle, price, quantity, image))
                    }
                }

                if (cartItems.isEmpty()) {
                    Toast.makeText(this@CartActivity, "Your cart is empty.", Toast.LENGTH_SHORT).show()
                }

                setupRecyclerView(cartItems)
                showLoading(false)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CartActivity, "Failed to load cart", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView(cartItems: List<CartItem>) {
        cartAdapter.updateCart(cartItems)
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerViewCart.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun getUserIdFromSession(): String? {
        return SessionManager.getUserId(this)
    }
}
