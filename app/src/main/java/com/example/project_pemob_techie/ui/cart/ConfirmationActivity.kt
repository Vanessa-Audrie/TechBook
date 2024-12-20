package com.example.project_pemob_techie.ui.cart

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.account.SessionManager
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ConfirmationActivity : AppCompatActivity() {
    private lateinit var confirmationRecyclerView: RecyclerView
    private lateinit var textViewName: TextView
    private lateinit var textViewPhoneNumber: TextView
    private lateinit var textViewAddress: TextView
    private lateinit var textViewTotalQuantity: TextView
    private lateinit var textViewTotalPrice: TextView
    private lateinit var textView47: TextView
    private lateinit var confAdapter: ConfirmationAdapter
    private lateinit var TotalamountTextview: TextView
    private lateinit var userId: String
    private val dbHelper by lazy { SQLiteHelper(this) }

    private var totalQuantity = 0

    fun onBackPressed(view: View) {
        Log.d("BackButton", "Back button clicked")
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        userId = SessionManager.getUserId(this) ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        confirmationRecyclerView = findViewById(R.id.recyclerViewitem)
        textViewName = findViewById(R.id.textView34)
        textViewPhoneNumber = findViewById(R.id.textView35)
        textViewAddress = findViewById(R.id.textView36)
        textViewTotalQuantity = findViewById(R.id.textView45)
        textViewTotalPrice = findViewById(R.id.textView46)
        textView47 = findViewById(R.id.textView47)
        TotalamountTextview = findViewById(R.id.textView111)
        confirmationRecyclerView.layoutManager = LinearLayoutManager(this)


        loadShippingDetails()
        loadSelectedItemsFromSQLite()
        val confirmButton: Button = findViewById(R.id.button4)
        confirmButton.setOnClickListener {
            val shippingFeeText = textView47.text.toString()
            val cleanedShippingFeeText = shippingFeeText.replace("[^\\d]".toRegex(), "")
            val shippingFee = if (cleanedShippingFeeText.isNotEmpty()) {
                cleanedShippingFeeText.toDoubleOrNull() ?: 0.0
            } else {
                0.0
            }
            val totalPriceText = textViewTotalPrice.text.toString()
            val cleanedTotalPriceText = totalPriceText.replace("[^\\d]".toRegex(), "")
            val totalPrice = if (cleanedTotalPriceText.isNotEmpty()) {
                cleanedTotalPriceText.toDoubleOrNull() ?: 0.0
            } else {
                0.0
            }
            Log.d("ConfirmationActivity", "Shipping Fee: $shippingFee, Total Price: $totalPrice")
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("totalPrice", totalPrice)
            intent.putExtra("shippingFee", shippingFee)
            startActivity(intent)
        }
    }

    private fun loadSelectedItemsFromSQLite() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val selectedItems = dbHelper.getAllSelectedItems()
                loadItemDetailsFromFirebase(selectedItems) { totalMass ->
                    this@ConfirmationActivity.totalMass = totalMass
                    checkAndLoadShippingFee()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ConfirmationActivity, "Error loading selected items: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private var totalMass = 0.0
    private var city: String? = null
    private var isCityLoaded = false
    private var isMassLoaded = false

    private fun loadItemDetailsFromFirebase(selectedItems: List<String>, callback: (Double) -> Unit) {
        

        val databaseRef = FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("3/cart/userId/$userId")
        val updatedItems = mutableListOf<CartItem>()
        val totalItems = selectedItems.size
        var itemsLoaded = 0
        var totalPrice = 0.0
        var totalMass = 0.0

        selectedItems.forEach { isbn ->
            databaseRef.child(isbn).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val title = snapshot.child("title").getValue(String::class.java).orEmpty()
                        val priceString = snapshot.child("price").getValue(String::class.java).orEmpty()
                        val price = priceString.toDoubleOrNull() ?: 0.0
                        val quantity = snapshot.child("quantity").getValue(Int::class.java) ?: 0
                        val image = snapshot.child("image").getValue(String::class.java).orEmpty()
                        val massRaw = snapshot.child("mass").value
                        val mass = when (massRaw) {
                            is Long -> massRaw.toDouble()
                            is Double -> massRaw
                            is String -> massRaw.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }
                        totalMass += mass * quantity
                        val cartItem = CartItem(
                            isbn = isbn,
                            title = title,
                            price = priceString,
                            quantity = quantity,
                            mass = mass,
                            image = image
                        )
                        updatedItems.add(cartItem)
                        itemsLoaded++
                        totalQuantity += quantity
                        totalPrice += price * quantity
                        if (itemsLoaded == totalItems) {
                            updateRecyclerView(updatedItems)
                            updateTotals(totalQuantity, totalPrice, 0.0)
                            isMassLoaded = true
                            callback(totalMass)
                        }
                        Log.d("ConfirmationActivity", "Item ISBN: $isbn, Mass: $mass, Quantity: $quantity, Total Mass: $totalMass")

                    } catch (e: Exception) {
                        Log.e("ConfirmationActivity", "Error loading item data: ${e.message}", e)
                        Toast.makeText(this@ConfirmationActivity, "Error loading item data", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("ConfirmationActivity", "Database error: ${error.message}")
                    Toast.makeText(this@ConfirmationActivity, "Failed to load item details", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun checkAndLoadShippingFee() {
        if (isCityLoaded && isMassLoaded && totalMass > 0) {
            val city = this.city ?: return
            loadShippingFee(city, totalMass)
            Log.d("ConfirmationActivity", "Total Mass: $totalMass")

        }
    }

    private fun loadShippingDetails() {
        
        val databaseRef = FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("10/shipping/$userId")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").value.toString()
                val phoneNumber = snapshot.child("phoneNumber").value.toString()
                val streetAddress = snapshot.child("streetAddress").value.toString()
                val city = snapshot.child("city").value.toString()
                val postalCode = snapshot.child("postalCode").value.toString()
                textViewName.text = name
                textViewPhoneNumber.text = phoneNumber
                textViewAddress.text = "$streetAddress, $city, $postalCode"
                this@ConfirmationActivity.city = city
                isCityLoaded = true
                checkAndLoadShippingFee()
            }
            override fun onCancelled(error: DatabaseError) {
                textViewName.text = "Failed to load shipping details"
            }
        })
    }

    private fun loadShippingFee(city: String, totalMass: Double) {
        
        val shippingFeeRef = FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("11/shipping_fee/$city")
        shippingFeeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val shippingFee = snapshot.child("shipping_fee").getValue(Double::class.java) ?: 0.0
                val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
                val totalPriceText = textViewTotalPrice.text.toString()
                val cleanedTotalPriceText = totalPriceText.replace("[^\\d]".toRegex(), "")
                val totalPrice = if (cleanedTotalPriceText.isNotEmpty()) {
                    cleanedTotalPriceText.toDoubleOrNull() ?: 0.0
                } else {
                    0.0
                }
                val roundedMass = kotlin.math.ceil(totalMass)
                Log.d("ConfirmationActivity", "Total Mass: $roundedMass")
                val shippingFee2 = roundedMass * shippingFee
                textView47.text = "Rp ${formatter.format(shippingFee2)}"
                updateTotals(totalQuantity, totalPrice, shippingFee2)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ConfirmationActivity, "Failed to load shipping fee", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun updateRecyclerView(updatedItems: List<CartItem>) {
        if (!::confAdapter.isInitialized) {
            confAdapter = ConfirmationAdapter(this, updatedItems.toMutableList())
            confirmationRecyclerView.adapter = confAdapter
        } else {
            confAdapter.selectedItems = updatedItems.toMutableList()
            confAdapter.notifyDataSetChanged()
        }
    }

    private fun updateTotals(totalQuantity: Int, totalPrice: Double, shippingFee2: Double) {
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        textViewTotalQuantity.text = "$totalQuantity"
        textViewTotalPrice.text = "Rp ${formatter.format(totalPrice)}"
        val totalAmount = totalPrice + shippingFee2
        TotalamountTextview.text = "Rp ${formatter.format(totalAmount)}"
    }

}
