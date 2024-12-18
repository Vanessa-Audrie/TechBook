package com.example.project_pemob_techie.ui.cart

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.project_pemob_techie.R
import java.text.NumberFormat
import java.util.Locale

class PaymentActivity : AppCompatActivity() {

    private lateinit var imageViewQRCode: ImageView
    private lateinit var textViewTotalAmount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_scan)

        imageViewQRCode = findViewById(R.id.imageView17)
        textViewTotalAmount = findViewById(R.id.textView50)

        val totalPrice = intent.getDoubleExtra("totalPrice", 0.0)
        val shippingFee = intent.getDoubleExtra("shippingFee", 0.0)

        val totalAmount = totalPrice + shippingFee

        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        textViewTotalAmount.text = "Payment due: Rp ${formatter.format(totalAmount)}"


        imageViewQRCode.setImageResource(R.drawable.qr)

        val confirmButton2: Button = findViewById(R.id.button5)
        confirmButton2.setOnClickListener {
            val intent = Intent(this, PaymentUpload::class.java)
            intent.putExtra("totalAmount", totalAmount)
            intent.putExtra("shippingFee", shippingFee)
            startActivity(intent)
        }

        val backBtn = findViewById<ImageView>(R.id.imageView15)
        backBtn.setOnClickListener {
            finish()
        }
    }
}
