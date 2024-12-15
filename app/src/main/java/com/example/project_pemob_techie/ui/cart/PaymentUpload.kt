package com.example.project_pemob_techie.ui.cart

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project_pemob_techie.R
import com.example.project_pemob_techie.ui.account.SessionManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class PaymentUpload : AppCompatActivity() {

    private lateinit var imageViewProof: ImageView
    private lateinit var uploadButton: Button
    private lateinit var submitButton: Button
    private var imageUri: Uri? = null
    private lateinit var databaseReference: DatabaseReference
    private var bitmap: Bitmap? = null
    private var userId: String? = null
    private lateinit var transactionId: String
    private lateinit var sqliteHelper: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_proof)

        val totalAmount = intent.getDoubleExtra("totalAmount", 0.0)
        val shippingFee = intent.getDoubleExtra("shippingFee", 0.0)

        imageViewProof = findViewById(R.id.imageViewProof)
        uploadButton = findViewById(R.id.button8)
        submitButton = findViewById(R.id.button6)
        sqliteHelper = SQLiteHelper(this)

        userId = SessionManager.getUserId(this) ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val backBtn = findViewById<ImageView>(R.id.imageView18)
        backBtn.setOnClickListener(){
            finish()
        }

        transactionId = UUID.randomUUID().toString()

        databaseReference = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("6/transaction/$userId/$transactionId")

        uploadButton.setOnClickListener {
            selectImage()
        }

        submitButton.setOnClickListener {
            uploadProof(totalAmount, shippingFee)
            val intent = Intent(this, PaymentDone::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                imageViewProof.setImageBitmap(bitmap)
            } catch (e: IOException) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadProof(totalAmount: Double, shippingFee: Double) {
        if (bitmap == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }
        val base64Image = encodeImageToBase64(bitmap!!)

        val paymentProof = mapOf(
            "imageBase64" to base64Image
        )

        val transactionData = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "totalAmount" to totalAmount,
            "shippingFee" to shippingFee,
            "payment_status" to false,
            "shipping_status" to false,
            "payment_proof" to paymentProof
        )

        databaseReference.setValue(transactionData)
            .addOnCompleteListener {
                clearCartAndSQLite()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save proof: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val resizedBitmap = resizeImage(bitmap, 1024, 1024)
        val byteArrayOutputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun resizeImage(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val scaleFactor = Math.min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun clearCartAndSQLite() {
        val cartReference = FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("3/cart/userId/$userId")

        cartReference.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val selectedItemsFromSQLite = sqliteHelper.getAllSelectedItems()

                val itemsToMove = mutableMapOf<String, Map<String, Any>>()
                dataSnapshot.children.forEach { itemSnapshot ->
                    val isbn = itemSnapshot.key
                    isbn?.let {
                        if (selectedItemsFromSQLite.contains(it)) {
                            val itemDetails = itemSnapshot.value as? Map<String, Any> ?: return@forEach
                            itemsToMove[it] = itemDetails
                        }
                    }
                }

                if (itemsToMove.isNotEmpty()) {
                    FirebaseDatabase.getInstance("https://techbook-f7669-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference("7/transaction_details/$transactionId").setValue(itemsToMove)
                        .addOnCompleteListener {
                            Toast.makeText(this, "Transaction details saved", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save transaction details: ${it.message}", Toast.LENGTH_SHORT).show()
                        }

                    itemsToMove.keys.forEach { isbn ->
                        cartReference.child(isbn).removeValue()
                        sqliteHelper.removeSelectedItem(isbn)
                    }
                } else {
                    Toast.makeText(this, "No matching items found in both cart and SQLite", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No items found in the cart", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch cart items: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
