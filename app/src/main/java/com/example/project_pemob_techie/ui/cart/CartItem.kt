package com.example.project_pemob_techie.ui.cart

import androidx.room.Entity
import androidx.room.PrimaryKey

data class CartItem(
    val bookId: String,
    val bookTitle: String,
    val price: String,
    var quantity: Int,
    var selected: Boolean = false
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val bookTitle: String,
    val price: String,
    val quantity: Int,
    val image: String?,
    val selected: Boolean
)
