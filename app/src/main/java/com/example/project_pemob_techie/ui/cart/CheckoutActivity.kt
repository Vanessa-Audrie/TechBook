package com.example.project_pemob_techie.ui.cart

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.account.SessionManager
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.common.reflect.TypeToken
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.coroutines.launch

class CheckoutActivity : AppCompatActivity() {

    private lateinit var cityDropdown: MaterialAutoCompleteTextView
    private val cities = listOf(
        "Medan Helvetia", "Medan Marelan", "Medan Selayang", "Medan Johor",
        "Medan Amplas", "Medan Tuntungan", "Medan Denai", "Medan Area",
        "Medan Kota", "Medan Polonia", "Medan Baru", "Medan Sunggal",
        "Medan Petisah", "Medan Barat", "Medan Timur", "Medan Perjuangan",
        "Medan Tembung", "Medan Deli", "Medan Labuhan", "Medan Belawan", "Medan Maimun"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shipping)

        val cityDropdown: MaterialAutoCompleteTextView = findViewById(R.id.dropdown)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)
        cityDropdown.setAdapter(adapter)

        val nameTextView: TextView = findViewById(R.id.textView25)
        val phoneNumberTextView: TextView = findViewById(R.id.textView28)
        val streetAddressTextView: TextView = findViewById(R.id.textView29)
        val postalCodeEditText: EditText = findViewById(R.id.editTextText2)

        val userId = SessionManager.getUserId(this)
        if (userId != null) {
            loadShippingDetailsFromFirebase(userId, nameTextView, phoneNumberTextView, streetAddressTextView, cityDropdown, postalCodeEditText)
        }

        val saveButton = findViewById<Button>(R.id.button2)
        saveButton.setOnClickListener {
            val name = nameTextView.text.toString().trim()
            val phoneNumber = phoneNumberTextView.text.toString().trim()
            val streetAddress = streetAddressTextView.text.toString().trim()
            val postalCode = postalCodeEditText.text.toString().trim()
            val selectedCity = cityDropdown.text.toString().trim()

            if (validateInput(name, phoneNumber, streetAddress, selectedCity, postalCode)) {
                saveShippingDetailsToFirebase(name, phoneNumber, streetAddress, selectedCity, postalCode)
            }
        }

        val backButton: ImageView = findViewById(R.id.imageView11)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(name: String, phoneNumber: String, streetAddress: String, city: String, postalCode: String): Boolean {
        return name.isNotEmpty() && phoneNumber.isNotEmpty() && streetAddress.isNotEmpty() && city.isNotEmpty() && postalCode.isNotEmpty()
    }

    private fun saveShippingDetailsToFirebase(name: String, phoneNumber: String, streetAddress: String, city: String, postalCode: String) {
        val userId = SessionManager.getUserId(this) ?: return
        val shippingDetails = mapOf(
            "name" to name,
            "phoneNumber" to phoneNumber,
            "streetAddress" to streetAddress,
            "city" to city,
            "postalCode" to postalCode
        )
        val databaseRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("10/shipping/$userId")

        databaseRef.setValue(shippingDetails).addOnSuccessListener {
            transferToConfirmation(name, phoneNumber, streetAddress, city, postalCode)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to save shipping details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadShippingDetailsFromFirebase(
        userId: String,
        nameTextView: TextView,
        phoneNumberTextView: TextView,
        streetAddressTextView: TextView,
        cityDropdown: MaterialAutoCompleteTextView,
        postalCodeEditText: EditText
    ) {
        val databaseRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("10/shipping/$userId")

        databaseRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val name = snapshot.child("name").value.toString()
                val phoneNumber = snapshot.child("phoneNumber").value.toString()
                val streetAddress = snapshot.child("streetAddress").value.toString()
                val postalCode = snapshot.child("postalCode").value.toString()
                val city = snapshot.child("city").value.toString()

                nameTextView.text = name
                phoneNumberTextView.text = phoneNumber
                streetAddressTextView.text = streetAddress
                postalCodeEditText.setText(postalCode)

                cityDropdown.setText(city, false)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load shipping details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun transferToConfirmation(name: String, phoneNumber: String, streetAddress: String, city: String, postalCode: String) {
        val intent = Intent(this, ConfirmationActivity::class.java).apply {
            putExtra("name", name)
            putExtra("phoneNumber", phoneNumber)
            putExtra("streetAddress", streetAddress)
            putExtra("postalCode", postalCode)
            putExtra("city", city)
        }
        startActivity(intent)
    }
}





