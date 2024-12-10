package com.example.project_pemob_techie.ui.cart

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.account.SessionManager
import com.example.project_pemob_techie.ui.content.CartAdapter
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ConfirmationActivity : AppCompatActivity() {

    private lateinit var confirmationRecyclerView: RecyclerView
    private lateinit var textViewName: TextView
    private lateinit var textViewPhoneNumber: TextView
    private lateinit var textViewAddress: TextView
    private lateinit var textViewTotalQuantity: TextView
    private lateinit var textViewTotalPrice: TextView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var selectedItems: ArrayList<CartItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        textViewName = findViewById(R.id.textView34)
        textViewPhoneNumber = findViewById(R.id.textView35)
        textViewAddress = findViewById(R.id.textView36)
        textViewTotalQuantity = findViewById(R.id.textView45)
        textViewTotalPrice = findViewById(R.id.textView46)
//        confirmationRecyclerView = findViewById(R.id.recyclerViewConfirmation)

        selectedItems = intent.getParcelableArrayListExtra("selectedItems") ?: ArrayList()

        // Set up the RecyclerView to display the cart items
//        cartAdapter = CartAdapter(this, selectedItems)
        confirmationRecyclerView.layoutManager = LinearLayoutManager(this)
        confirmationRecyclerView.adapter = cartAdapter

        loadShippingDetails()

        calculateTotals()
    }

    private fun loadShippingDetails() {
        val userId = SessionManager.getUserId(this)
        if (userId.isNullOrEmpty()) {
            textViewName.text = "User not logged in"
            return
        }

        val databaseRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("10/shipping/$userId")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").value.toString()
                val phoneNumber = snapshot.child("phoneNumber").value.toString()
                val streetAddress = snapshot.child("streetAddress").value.toString()
                val postalCode = snapshot.child("postalCode").value.toString()

                textViewName.text = name
                textViewPhoneNumber.text = phoneNumber
                textViewAddress.text = "$streetAddress, $postalCode"
            }

            override fun onCancelled(error: DatabaseError) {
                textViewName.text = "Failed to load shipping details"
            }
        })
    }

    private fun calculateTotals() {
        var totalQuantity = 0
        var totalPrice = 0.0

        for (item in selectedItems) {
            totalQuantity += item.quantity
//            totalPrice += item.price * item.quantity
        }

        textViewTotalQuantity.text = "Total Quantity: $totalQuantity"
        textViewTotalPrice.text = "Total Price: $totalPrice"
    }
}
