package com.example.project_pemob_techie.ui.content

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project_pemob_techie.R
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class Shipping : AppCompatActivity() {

    private val cities = listOf(
        "Medan Helvetia", "Medan Marelan", "Medan Selayang", "Medan Johor",
        "Medan Amplas", "Medan Tuntungan", "Medan Denai", "Medan Area",
        "Medan Kota", "Medan Polonia", "Medan Baru", "Medan Sunggal",
        "Medan Petisah", "Medan Barat", "Medan Timur", "Medan Perjuangan",
        "Medan Tembung", "Medan Deli", "Medan Labuhan", "Medan Belawan", "Medan Maimun"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_fee)

        val cityDropdown: MaterialAutoCompleteTextView = findViewById(R.id.dropdown2)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)
        cityDropdown.setAdapter(adapter)

        val backButton = findViewById<ImageView>(R.id.imageView7)
        val shippingFeeTextView = findViewById<TextView>(R.id.textView16)

        backButton.setOnClickListener {
            finish()
        }

        cityDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedCity = cities[position]
            fetchShippingFee(selectedCity, shippingFeeTextView)
        }
    }

    private fun fetchShippingFee(city: String, shippingFeeTextView: TextView) {
        val shippingFeeRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("11/shipping_fee/$city")

        shippingFeeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val shippingFee = snapshot.child("shipping_fee").getValue(Double::class.java) ?: 0.0
                val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
                shippingFeeTextView.text = "Rp ${formatter.format(shippingFee)}"
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Shipping, "Failed to load shipping fee", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
