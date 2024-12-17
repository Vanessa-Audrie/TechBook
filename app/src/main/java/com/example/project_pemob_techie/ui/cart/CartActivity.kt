package com.example.project_pemob_techie.ui.cart

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.account.SessionManager
import com.example.project_pemob_techie.ui.content.CartAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerViewCart: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var cartAdapter: CartAdapter
    private lateinit var checkbox2: CheckBox
    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var userId: String
    private val dbHelper by lazy { SQLiteHelper(this) }

    private lateinit var textView12: TextView
    private lateinit var textView13: TextView
    private lateinit var tvStatus: TextView

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

        checkbox2 = findViewById(R.id.checkBox2)
        recyclerViewCart = findViewById(R.id.viewCart)
        textView12 = findViewById(R.id.textView12)
        textView13 = findViewById(R.id.textView13)
        tvStatus = findViewById(R.id.tvstatus)

        cartAdapter = CartAdapter(this, mutableListOf(), textView12, textView13, checkbox2)

        progressBar = findViewById(R.id.progressBar2)
        recyclerViewCart.layoutManager = LinearLayoutManager(this)
        recyclerViewCart.adapter = cartAdapter

        checkbox2.setOnCheckedChangeListener { _, isChecked ->
            cartAdapter.selectAllItems(isChecked)
        }

        cartViewModel.cartItems.observe(this, { items ->
            cartAdapter.updateCart(items)
            updateTvStatusVisibility(items.isEmpty())
        })

        cartViewModel.loadCartItems(userId)

        val checkoutButton: Button = findViewById(R.id.button3)
        checkoutButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val selectedItems = dbHelper.getAllSelectedItems()
                withContext(Dispatchers.Main) {
                    if (selectedItems.isNotEmpty()) {
                        val intent = Intent(this@CartActivity, CheckoutActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@CartActivity, "Please select at least one item to checkout.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateTvStatusVisibility(isCartEmpty: Boolean) {
        tvStatus.visibility = if (isCartEmpty) View.VISIBLE else View.GONE
    }

    private fun getUserIdFromSession(): String? {
        return SessionManager.getUserId(this)
    }
}

