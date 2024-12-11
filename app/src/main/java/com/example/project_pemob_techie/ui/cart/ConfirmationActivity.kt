package com.example.project_pemob_techie.ui.cart

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.account.SessionManager
import com.google.firebase.database.*

class ConfirmationActivity : AppCompatActivity() {

    private lateinit var confirmationRecyclerView: RecyclerView
    private lateinit var textViewName: TextView
    private lateinit var textViewPhoneNumber: TextView
    private lateinit var textViewAddress: TextView
    private lateinit var textViewTotalQuantity: TextView
    private lateinit var textViewTotalPrice: TextView
    private lateinit var confAdapter: ConfirmationAdapter
    private var selectedItems: MutableList<CartItem> = mutableListOf()
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        confirmationRecyclerView = findViewById(R.id.recyclerViewitem)
        textViewName = findViewById(R.id.textView34)
        textViewPhoneNumber = findViewById(R.id.textView35)
        textViewAddress = findViewById(R.id.textView36)
        textViewTotalQuantity = findViewById(R.id.textView45)
        textViewTotalPrice = findViewById(R.id.textView46)

        confirmationRecyclerView.layoutManager = LinearLayoutManager(this)

        userId = SessionManager.getUserId(this) ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadShippingDetails()

        val isbnList = intent.getStringArrayListExtra("isbnList") ?: ArrayList()
        if (isbnList.isNotEmpty()) {
            loadSelectedItems(isbnList)
        }

        val backButton: ImageView = findViewById(R.id.imageView13)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadShippingDetails() {
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

    private fun loadSelectedItems(isbnList: ArrayList<String>) {
        val databaseRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("3/cart")

        var loadedItemsCount = 0

        val userId = SessionManager.getUserId(this) ?: return

        isbnList.forEach { isbn ->
            databaseRef.child(userId).child(isbn).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val bookTitle = snapshot.child("title").value.toString()
                    val price = snapshot.child("price").value.toString()
                    val image = snapshot.child("image").value.toString()
                    val quantity = snapshot.child("quantity").value as Int

                    val cartItem = CartItem(isbn, bookTitle, price, quantity, image)
                    selectedItems.add(cartItem)
                }

                loadedItemsCount++

                if (loadedItemsCount == isbnList.size) {
                    updateRecyclerView()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load item with ISBN $isbn", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateRecyclerView() {
        if (!::confAdapter.isInitialized) {
            confAdapter = ConfirmationAdapter(this, selectedItems)
            confirmationRecyclerView.adapter = confAdapter
        } else {
            confAdapter.selectedItems = selectedItems as ArrayList<CartItem>
            confAdapter.notifyDataSetChanged()
        }
    }
}

