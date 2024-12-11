package com.example.project_pemob_techie.ui.cart

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.account.SessionManager
import com.google.common.reflect.TypeToken
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.coroutines.launch

class CheckoutActivity : AppCompatActivity() {

    private lateinit var selectedItems: List<CartItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shipping)

        val selectedISBNs = loadSelectedItemISBNsLocally()
        fetchSelectedItemsDetails(selectedISBNs)

        val saveButton = findViewById<Button>(R.id.button2)
        saveButton.setOnClickListener {
            val name = findViewById<TextView>(R.id.textView25).text.toString().trim()
            val phoneNumber = findViewById<TextView>(R.id.textView28).text.toString().trim()
            val streetAddress = findViewById<TextView>(R.id.textView29).text.toString().trim()
            val postalCode = findViewById<EditText>(R.id.editTextText2).text.toString().trim()

            if (validateInput(name, phoneNumber, streetAddress, postalCode)) {
                saveShippingDetailsToFirebase(name, phoneNumber, streetAddress, postalCode)
            }
        }

        val backButton: ImageView = findViewById(R.id.imageView11)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchSelectedItemsDetails(selectedISBNs: List<String>) {
        val userId = SessionManager.getUserId(this) ?: return
        val databaseRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("3/cart/userId/$userId")

        selectedItems = mutableListOf()

        selectedISBNs.forEach { isbn ->
            databaseRef.child(isbn).get().addOnSuccessListener { snapshot ->
                snapshot?.getValue(CartItem::class.java)?.let { item ->
                    (selectedItems as MutableList).add(item)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to fetch item details for ISBN: $isbn", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun loadSelectedItemISBNsLocally(): List<String> {
        val sharedPreferences = getSharedPreferences("cartPrefs", MODE_PRIVATE)
        val json = sharedPreferences.getString("selectedItemISBNs", "[]")
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun validateInput(name: String, phoneNumber: String, streetAddress: String, postalCode: String): Boolean {
        return true
    }

    private fun saveShippingDetailsToFirebase(name: String, phoneNumber: String, streetAddress: String, postalCode: String) {
        val userId = SessionManager.getUserId(this) ?: return
        val shippingDetails = mapOf(
            "name" to name,
            "phoneNumber" to phoneNumber,
            "streetAddress" to streetAddress,
            "postalCode" to postalCode
        )

        val databaseRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("10/shipping/$userId")

        databaseRef.setValue(shippingDetails).addOnSuccessListener {
            transferSelectedItemsToConfirmation(name, phoneNumber, streetAddress, postalCode)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to save shipping details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun transferSelectedItemsToConfirmation(name: String, phoneNumber: String, streetAddress: String, postalCode: String) {
        val intent = Intent(this, ConfirmationActivity::class.java).apply {
            putExtra("name", name)
            putExtra("phoneNumber", phoneNumber)
            putExtra("streetAddress", streetAddress)
            putExtra("postalCode", postalCode)

            val selectedISBNs = loadSelectedItemISBNsLocally()

            putStringArrayListExtra("isbnList", ArrayList(selectedISBNs))


        }
        startActivity(intent)
    }



}



