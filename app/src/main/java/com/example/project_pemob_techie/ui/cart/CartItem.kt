package com.example.project_pemob_techie.ui.cart

data class CartItem(
    val isbn: String = "",
    val title: String? = null,
    val price: String? = null,
    var quantity: Int,
    var image: String? = null,
    var selected: Boolean = false
)
