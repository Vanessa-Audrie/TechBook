package com.example.project_pemob_techie.ui.cart;

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CartRepository(context: Context) {

    private val cartDao: CartDao = CartDatabase.getDatabase(context).cartDao()

    private fun mapToCartItem(cartItemEntity: CartItemEntity): CartItem {
        return CartItem(
            cartItemEntity.bookId,
            cartItemEntity.bookTitle,
            cartItemEntity.price,
            cartItemEntity.quantity,
            cartItemEntity.selected
        )
    }
    private fun mapToCartItemEntity(cartItem: CartItemEntity): CartItemEntity {
        return CartItemEntity(
            bookId = cartItem.bookId,
            bookTitle = cartItem.bookTitle,
            price = cartItem.price,
            quantity = cartItem.quantity,
            image = cartItem.image,
            selected = cartItem.selected
        )
    }

    suspend fun addCartItem(cartItem: CartItem) {
        withContext(Dispatchers.IO) {
            cartDao.insert(mapToCartItemEntity(cartItem))
        }
    }

    suspend fun getCartItems(): List<CartItem> {
        return withContext(Dispatchers.IO) {
            cartDao.getAll().map { mapToCartItem(it) }
        }
    }

    suspend fun deleteCartItem(cartItem: CartItemEntity) {
        withContext(Dispatchers.IO) {
            cartDao.delete(mapToCartItemEntity(cartItem))
        }
    }
}

