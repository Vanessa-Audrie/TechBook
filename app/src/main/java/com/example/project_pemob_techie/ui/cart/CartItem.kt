package com.example.project_pemob_techie.ui.cart

data class CartItem(
    val bookId: String = "",
    val book_title: String? = null,
    val price: String? = null,
    var quantity: Int = 1,
    var image: String? = null
)