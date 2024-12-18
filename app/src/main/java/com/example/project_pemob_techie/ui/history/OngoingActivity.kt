package com.example.project_pemob_techie.ui.history

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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
import com.itextpdf.html2pdf.HtmlConverter
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
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
        

        val shippingStatusRef = FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/")
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
                                sendInvoiceEmail()
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
        

        val transactionDetailsRef = FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/")
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
        
        val transactionRef = FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("6/transaction/$userId/$transactionId")

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
                    val yellowDrawable = GradientDrawable()
                    yellowDrawable.shape = GradientDrawable.RECTANGLE
                    yellowDrawable.cornerRadius = 16f
                    yellowDrawable.setColor(ContextCompat.getColor(this@OngoingActivity, R.color.yellow))
                    binding.btnReceived.background = yellowDrawable
                } else {
                    binding.btnReceived.isEnabled = false
                    val grayDrawable = GradientDrawable()
                    grayDrawable.shape = GradientDrawable.RECTANGLE
                    grayDrawable.cornerRadius = 16f
                    grayDrawable.setColor(ContextCompat.getColor(this@OngoingActivity, R.color.gray))

                    binding.btnReceived.background = grayDrawable

                }

                binding.tvPaymentPending.visibility = if (paymentStatus) View.GONE else View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OngoingActivity, "Failed to fetch transaction summary", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendInvoiceEmail() {
        if (transactionId == null || userId == null) {
            Toast.makeText(this, "Invalid transaction or user ID", Toast.LENGTH_SHORT).show()
            return
        }

        val transactionRef = FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("6/transaction/$userId/$transactionId")

        transactionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalAmount = snapshot.child("totalAmount").getValue(Long::class.java) ?: 0L
                val shippingFee = snapshot.child("shippingFee").getValue(Long::class.java) ?: 0L
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()

                val formattedDate = formatTimestamp(timestamp)
                val userRef = FirebaseDatabase.getInstance("https://techbook-by-techie-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("techbook_techie/user/$userId")

                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(userSnapshot: DataSnapshot) {
                        val email = userSnapshot.child("email").getValue(String::class.java)

                        if (email != null) {
                            val htmlContent = buildInvoiceHtml(transactionId!!,
                                totalAmount.toString(),
                                shippingFee.toString(),
                                itemQuantities,
                                formattedDate
                            )

                            val pdfFile = createPdfFromHtml(htmlContent)

                            val senderEmail = "techie.techbook@gmail.com"
                            val appPassword = "gqmp gaun pgxy eggb"

                            SendMailTask(
                                senderEmail = senderEmail,
                                appPassword = appPassword,
                                recipientEmail = email,
                                subject = "This Is Your TechBook Invoice",
                                htmlContent = htmlContent,
                                attachmentFile = pdfFile,
                                onSuccess = {
                                    Toast.makeText(this@OngoingActivity, "Invoice sent successfully!", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { e ->
                                    Log.e("EmailError", "Failed to send invoice", e)
                                    Toast.makeText(this@OngoingActivity, "Failed to send email: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            ).execute()
                        } else {
                            Toast.makeText(this@OngoingActivity, "User email not found", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@OngoingActivity, "Failed to fetch user email", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OngoingActivity, "Failed to fetch transaction data", Toast.LENGTH_SHORT).show()
            }
        })
    }


    fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH)
        val date = Date(timestamp)
        return dateFormat.format(date)
    }


    private fun createPdfFromHtml(htmlContent: String): File {
        val pdfFile = File(cacheDir, "invoice.pdf")

        try {
            val pdfOutputStream = FileOutputStream(pdfFile)
            HtmlConverter.convertToPdf(htmlContent, pdfOutputStream)
            Log.d("PDF", "PDF Created Successfully at: ${pdfFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("PDF", "Error generating PDF", e)
        }

        return pdfFile
    }

    private fun buildInvoiceHtml(
        transactionId: String,
        totalAmount: String,
        shippingFee: String,
        items: List<TransactionItem>,
        formattedDate: String
    ): String {
        fun formatCurrency(amount: String): String {
            val numberFormat = NumberFormat.getInstance(Locale("id", "ID"))
            return numberFormat.format(amount.toDouble())
        }

        val totalPrice = totalAmount.toDouble() - shippingFee.toDouble()
        val itemsHtml = items.joinToString("") { item ->
            """
            <tr>
                <td>${item.itemName}</td>
                <td>${item.quantity}</td>
                <td>Rp${formatCurrency(item.price)}</td>
            </tr>
            """
                }
                return """
        <html>
          <body>
            <p><b>Transaction ID:</b> $transactionId</p>
            <p><b>Transaction Date:</b> $formattedDate</p>
            <p>Here are your order details:</p>
            
            <table border="1" cellpadding="5" cellspacing="0">
              <tr>
                <th>Item</th>
                <th>Quantity</th>
                <th>Price</th>
              </tr>
              $itemsHtml
            </table>
            <p><b>Total Price:</b> Rp${formatCurrency(totalPrice.toString())}</p>
            <p><b>Shipping Fee:</b> Rp${formatCurrency(shippingFee)}</p>
            <p><b>Total Amount:</b> Rp${formatCurrency(totalAmount)}</p>
            <p>Thank you for shopping with us!</p>
            <p>Best regards, TechBook</p>
          </body>
        </html>
        """
    }

}

data class TransactionItem(
    val itemName: String = "",
    val quantity: Int = 0,
    val price: String = "",
    val imageHex: String = ""
)



