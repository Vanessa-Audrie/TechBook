package com.example.project_pemob_techie.ui.history

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.databinding.ActivityHistoryCompletedBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class CompletedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryCompletedBinding
    private var transactionId: String? = null
    private var userId: String? = null
    private val itemQuantities = mutableListOf<TransactionItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryCompletedBinding.inflate(layoutInflater)
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

        binding.btnReview.setOnClickListener {
            val intent = Intent(this, RatingReviewActivity::class.java)
            intent.putExtra("USER_ID", userId)
            intent.putExtra("TRANSACTION_ID", transactionId)
            startActivity(intent)
        }

        fetchTransactionSummary(transactionId!!)
        fetchTransactionDetails(transactionId!!)
        checkReviewStatus(userId!!, transactionId!!)
    }

    override fun onResume() {
        super.onResume()
        fetchTransactionSummary(transactionId!!)
        fetchTransactionDetails(transactionId!!)
        checkReviewStatus(userId!!, transactionId!!)
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
                    Log.e("CompletedActivity", "No items to display in RecyclerView.")
                    Toast.makeText(this@CompletedActivity, "No items found for this transaction.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CompletedActivity, "Failed to fetch transaction details", Toast.LENGTH_SHORT).show()
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
                val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
                binding.tvShippingFeeValue.text = "Rp ${formatter.format(shippingFee)}"
                binding.tvTotalsValue.text = "Rp ${formatter.format(totalAmount)}"
                val itemsTotal = totalAmount - shippingFee
                binding.tvItemsTotalValue.text = "Rp ${formatter.format(itemsTotal)}"
                binding.tvAddressContent.text = address
                binding.tvOrderStatus.text = "Package has been delivered!"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CompletedActivity, "Failed to fetch transaction summary", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkReviewStatus(userId: String, transactionId: String) {
        val reviewRef = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("5/rating_reviews")
        reviewRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var hasReviewed = false

                for (isbnSnapshot in snapshot.children) {
                    if (isbnSnapshot.hasChild(userId)) {
                        val userSnapshot = isbnSnapshot.child(userId)
                        if (userSnapshot.hasChild(transactionId)) {
                            hasReviewed = true
                            break
                        }
                    }
                }
                if (hasReviewed) {
                    binding.btnReview.isEnabled = false
                    binding.btnReview.setBackgroundColor(ContextCompat.getColor(this@CompletedActivity, R.color.gray))
                    binding.tvReviewStatus.visibility = android.view.View.VISIBLE
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CompletedActivity, "Failed to check review status", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
