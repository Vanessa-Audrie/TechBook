package com.example.project_pemob_techie.ui.content

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.project_pemob_techie.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class BookDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_page)

        val bookTitle = intent.getStringExtra("BOOK_TITLE")
        val bookPrice = intent.getStringExtra("BOOK_PRICE")
        val bookImage = intent.getStringExtra("BOOK_IMAGE")
        val bookSynopsis = intent.getStringExtra("BOOK_SYNOPSIS")
        val bookISBN = intent.getStringExtra("BOOK_ISBN")
        val bookAuthor = intent.getStringExtra("BOOK_AUTHOR")
        val bookLanguage = intent.getStringExtra("BOOK_LANG")
        val bookPages = intent.getStringExtra("BOOK_PAGES")
        val bookDate = intent.getStringExtra("BOOK_DATE")
        val bookMass = intent.getStringExtra("BOOK_MASS")
        val bookPublisher = intent.getStringExtra("BOOK_PUBLISHER")

        findViewById<TextView>(R.id.textView85).text = bookTitle
        findViewById<TextView>(R.id.textView86).text = "Rp $bookPrice"
//        findViewById<TextView>(R.id.isbnTextView).text = bookISBN
//        findViewById<TextView>(R.id.authorTextView).text = bookAuthor
//        findViewById<TextView>(R.id.languageTextView).text = bookLanguage
//        findViewById<TextView>(R.id.pagesTextView).text = bookPages
//        findViewById<TextView>(R.id.dateTextView).text = bookDate
//        findViewById<TextView>(R.id.massTextView).text = bookMass
//        findViewById<TextView>(R.id.publisherTextView).text = bookPublisher

        val tabLayout: TabLayout = findViewById(R.id.tabbedLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

        val adapter = BookDetailsPagerAdapter(this, bookSynopsis ?: "")
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Description"
                1 -> "Details"
                2 -> "Rating Reviews"
                else -> "Description"
            }
        }.attach()

        viewPager.currentItem = 0

        if (!bookImage.isNullOrEmpty()) {
            val imageBytes = hexStringToByteArray(bookImage)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            findViewById<ImageView>(R.id.bookImage).setImageBitmap(bitmap)
        }
    }

    private fun hexStringToByteArray(hexString: String): ByteArray {
        val result = ByteArray(hexString.length / 2)
        for (i in hexString.indices step 2) {
            val byte = hexString.substring(i, i + 2).toInt(16).toByte()
            result[i / 2] = byte
        }
        return result
    }
}
