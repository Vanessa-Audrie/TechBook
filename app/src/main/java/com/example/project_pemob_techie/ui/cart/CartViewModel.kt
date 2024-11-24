package com.example.project_pemob_techie.ui.cart
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


    class CartViewModel : ViewModel() {
        private val _cartItems = MutableLiveData<MutableList<CartItem>>(mutableListOf())
        val cartItems: LiveData<MutableList<CartItem>> get() = _cartItems

        fun addToCart(item: CartItem) {
            val currentCart = _cartItems.value ?: mutableListOf()
            val existingItem = currentCart.find { it.bookId == item.bookId }

            if (existingItem != null) {
                // Jika item sudah ada, tingkatkan jumlahnya
                existingItem.quantity += item.quantity
            } else {
                // Jika item belum ada, tambahkan ke list
                currentCart.add(item)
            }
            _cartItems.value = currentCart
        }
    }
