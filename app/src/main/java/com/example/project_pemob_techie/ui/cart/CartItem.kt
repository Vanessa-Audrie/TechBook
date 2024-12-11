package com.example.project_pemob_techie.ui.cart

data class CartItem(
    val isbn: String = "",
    val title: String? = null,
    val price: String? = null,
    var quantity: Int = 0,
    var image: String? = null,
    var selected: Boolean = false
) {
    constructor() : this(isbn = "", title = null, price = null, quantity = 0, image = null, selected = false)
}
