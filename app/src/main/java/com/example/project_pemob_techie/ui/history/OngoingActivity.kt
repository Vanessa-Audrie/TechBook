package com.example.project_pemob_techie.ui.history

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.databinding.ActivityHistoryOngoingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale

class OngoingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryOngoingBinding
    private var transactionId: String? = null
    private var userId: String? = null
    private val itemQuantities = mutableListOf<TransactionItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryOngoingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionId = intent.getStringExtra("TRANSACTION_ID")
        userId = intent.getStringExtra("USER_ID")

        if (transactionId == null || userId == null) {
            Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show()
            return
        }
        binding.rvItems.layoutManager = LinearLayoutManager(this)

        binding.tvOrderTitle.text = "$transactionId"

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        binding.imageView3.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        fetchTransactionSummary(transactionId!!)
        fetchTransactionDetails(transactionId!!)

        binding.btnReceived.setOnClickListener {
            showConfirmationDialog()
        }
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Receipt")
            .setMessage("Are you sure you have received the package?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                markAsReceived()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()

        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_box)

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton?.setTextColor(Color.YELLOW)

        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton?.setTextColor(Color.YELLOW)

        dialog.show()
    }



    private fun markAsReceived() {
        if (transactionId == null || userId == null) {
            Toast.makeText(this, "Invalid transaction or user ID", Toast.LENGTH_SHORT).show()
            return
        }

        val shippingStatusRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("6/transaction/$userId/$transactionId/shipping_status")

        shippingStatusRef.setValue(true).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Marked as Received", Toast.LENGTH_SHORT).show()
                binding.btnReceived.isEnabled = false
                binding.tvOrderStatus.text = "Order Received"
                recreate()

                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    user.getIdToken(true).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val idToken = task.result?.token
                            if (idToken != null) {
                                sendTokenToMake(idToken)
                                finish()
                            } else {
                                Toast.makeText(this, "Failed to get ID token", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Failed to retrieve ID token", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Failed to update shipping status. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun fetchTransactionDetails(transactionId: String) {
        val transactionDetailsRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("7/transaction_details/$transactionId")

        transactionDetailsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<TransactionItem>()
                itemQuantities.clear()

                for (isbnSnapshot in snapshot.children) {
                    val itemName = isbnSnapshot.child("title").getValue(String::class.java) ?: "Unknown"
                    val quantity = isbnSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                    val priceString = isbnSnapshot.child("price").getValue(String::class.java) ?: "0.0"
                    val imageHex = isbnSnapshot.child("image").getValue(String::class.java) ?: ""

                    val item = TransactionItem(itemName, quantity, priceString, imageHex)
                    items.add(item)
                    itemQuantities.add(item)

                    binding.tvItemsSelectedValue.text = "${itemQuantities.sumOf { it.quantity }}"
                }

                if (items.isNotEmpty()) {
                    val adapter = ItemsAdapter(items)
                    binding.rvItems.adapter = adapter
                } else {
                    Log.e("OngoingActivity", "No items to display in RecyclerView.")
                    Toast.makeText(this@OngoingActivity, "No items found for this transaction.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OngoingActivity, "Failed to fetch transaction details", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchTransactionSummary(transactionId: String) {
        val transactionRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("6/transaction/$userId/$transactionId")

        transactionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val shippingFeeLong = snapshot.child("shippingFee").getValue(Long::class.java) ?: 0L
                val shippingFee = shippingFeeLong.toDouble()

                val totalAmountLong = snapshot.child("totalAmount").getValue(Long::class.java) ?: 0L
                val totalAmount = totalAmountLong.toDouble()

                val address = snapshot.child("address").getValue(String::class.java) ?: "No address available"
                val paymentStatus = snapshot.child("payment_status").getValue(Boolean::class.java) ?: false

                val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))

                binding.tvShippingFeeValue.text = "Rp ${formatter.format(shippingFee)}"
                binding.tvTotalsValue.text = "Rp ${formatter.format(totalAmount)}"
                val itemsTotal = totalAmount - shippingFee
                binding.tvItemsTotalValue.text = "Rp ${formatter.format(itemsTotal)}"
                binding.tvAddressContent.text = address
                binding.tvOrderStatus.text = "Package is on the way!"
                if (paymentStatus) {
                    binding.btnReceived.isEnabled = true
                } else {
                    binding.btnReceived.isEnabled = false
                    binding.btnReceived.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.gray))

                }

                binding.tvPaymentPending.visibility = if (paymentStatus) View.GONE else View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OngoingActivity, "Failed to fetch transaction summary", Toast.LENGTH_SHORT).show()
            }
        })
    }



    fun sendTokenToMake(idToken: String) {
        val transactionRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("6/transaction/$userId/$transactionId")

        transactionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()

                val userRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("techbook_techie/user/$userId")

                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(userSnapshot: DataSnapshot) {
                        val email = userSnapshot.child("email").getValue(String::class.java)

                        if (email != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val url = URL("https://hook.us2.make.com/xyppeeiv1axnd6m7agifqojxh5pxwu36")
                                    val connection = url.openConnection() as HttpURLConnection
                                    connection.requestMethod = "POST"
                                    connection.setRequestProperty("Content-Type", "application/json")
                                    connection.doOutput = true

                                    val jsonInputString = """
                                    {
                                        "idToken": "$idToken",
                                        "transactionId": "$transactionId",
                                        "userId": "$userId",
                                        "timestamp": "$timestamp",
                                        "totalAmount": "${binding.tvTotalsValue.text}",
                                        "shippingFee": "${binding.tvShippingFeeValue.text}",
                                        "items": ${prepareItemsList()},
                                        "email": "$email"
                                    }
                                """

                                    OutputStreamWriter(connection.outputStream).apply {
                                        write(jsonInputString)
                                        flush()
                                    }

                                    val responseCode = connection.responseCode
                                    withContext(Dispatchers.Main) {
                                        if (responseCode == HttpURLConnection.HTTP_OK) {
                                            Log.d("HTTP Response", "Sent data successfully")
                                            Toast.makeText(this@OngoingActivity, "Data sent successfully", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Log.e("HTTP Response", "Error sending data")
                                            Toast.makeText(this@OngoingActivity, "Failed to send data", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Log.e("NetworkError", e.message ?: "Unknown error")
                                        Toast.makeText(this@OngoingActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this@OngoingActivity, "Failed to fetch email", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@OngoingActivity, "Failed to fetch user details", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OngoingActivity, "Failed to fetch transaction details", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun prepareItemsList(): String {
        val itemsList = mutableListOf<String>()
        itemQuantities.forEach { item ->
            itemsList.add("{\"itemName\": \"${item.itemName}\", \"quantity\": ${item.quantity}, \"price\": ${item.price}}")
        }
        return itemsList.joinToString(",")
    }
}

data class TransactionItem(
    val itemName: String = "",
    val quantity: Int = 0,
    val price: String = "",
    val imageHex: String = ""
)



