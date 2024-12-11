package com.example.project_pemob_techie.ui.cart
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


    class CartViewModel : ViewModel() {
        private val _cartItems = MutableLiveData<MutableList<CartItem>>(mutableListOf())
        val cartItems: LiveData<MutableList<CartItem>> get() = _cartItems

        fun addToCart(item: CartItem) {
            val currentCart = _cartItems.value ?: mutableListOf()
            val existingItem = currentCart.find { it.isbn == item.isbn }

            if (existingItem != null) {
                existingItem.quantity += item.quantity
            } else {
                currentCart.add(item)
            }
            _cartItems.value = currentCart
        }
    }
