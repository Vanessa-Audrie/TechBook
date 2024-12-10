package com.example.project_pemob_techie.ui.cart

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.account.SessionManager
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class CheckoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shipping)

        val selectedItems = intent.getParcelableArrayListExtra<CartItem>("selectedItems")

        val nameTextView = findViewById<TextView>(R.id.textView25)
        val phoneNumberTextView = findViewById<TextView>(R.id.textView28)
        val streetAddressTextView = findViewById<TextView>(R.id.textView29)
        val postalCodeEditText = findViewById<EditText>(R.id.editTextText2)
        val saveButton = findViewById<Button>(R.id.button2)

        saveButton.setOnClickListener {
            val name = nameTextView.text.toString().trim()
            val phoneNumber = phoneNumberTextView.text.toString().trim()
            val streetAddress = streetAddressTextView.text.toString().trim()
            val postalCode = postalCodeEditText.text.toString().trim()

            if (validateInput(name, phoneNumber, streetAddress, postalCode)) {
                saveShippingDetailsToFirebase(name, phoneNumber, streetAddress, postalCode, selectedItems)
            }
        }
    }

    private fun validateInput(name: String, phoneNumber: String, streetAddress: String, postalCode: String): Boolean {
        if (name.isEmpty() || phoneNumber.isEmpty() || streetAddress.isEmpty() || postalCode.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!phoneNumber.startsWith("62")) {
            Toast.makeText(this, "Phone number must start with 62", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveShippingDetailsToFirebase(name: String, phoneNumber: String, streetAddress: String, postalCode: String, selectedItems: ArrayList<CartItem>?) {
        val userId = SessionManager.getUserId(this)
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val shippingDetails = mapOf(
            "name" to name,
            "phoneNumber" to phoneNumber,
            "streetAddress" to streetAddress,
            "postalCode" to postalCode
        )

        val databaseRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("10/shipping/$userId")

        databaseRef.setValue(shippingDetails)
            .addOnSuccessListener {
                Toast.makeText(this, "Shipping details saved successfully", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, ConfirmationActivity::class.java).apply {
                    putParcelableArrayListExtra("selectedItems", selectedItems)
                }
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Failed to save shipping details: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}


//    // CheckoutActivity.kt
//    private fun saveOrderToFirebase(selectedItems: List<CartItem>) {
//        val userId = SessionManager.getUserId(this)
//        if (userId.isNullOrEmpty()) {
//            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val databaseRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("orders/$userId")
//        val orderMap = selectedItems.map {
//            mapOf(
//                "bookId" to it.bookId,
//                "bookTitle" to it.bookTitle,
//                "price" to it.price,
//                "quantity" to it.quantity,
//                "image" to it.image,
//                "selected" to it.selected
//            )
//        }
//
//        databaseRef.push().setValue(orderMap).addOnCompleteListener {
//            if (it.isSuccessful) {
//                Toast.makeText(this, "Order placed successfully", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show()
//            }
//        }
