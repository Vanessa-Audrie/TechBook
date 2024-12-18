package com.example.project_pemob_techie.ui.cart

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "cart_db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "selected_items"
        const val COLUMN_ISBN = "isbn"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ISBN TEXT PRIMARY KEY
            )
        """
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db.execSQL(dropTableQuery)
        onCreate(db)
    }

    fun addSelectedItem(isbn: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ISBN, isbn)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun removeSelectedItem(isbn: String) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ISBN = ?", arrayOf(isbn))
        db.close()
    }

    @SuppressLint("Range")
    fun getAllSelectedItems(): List<String> {
        val selectedItems = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.query(
            "selected_items",
            arrayOf("isbn"),
            null, null, null, null, null
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val isbn = cursor.getString(cursor.getColumnIndex("isbn"))
                selectedItems.add(isbn)
            } while (cursor.moveToNext())
        }

        cursor?.close()
        db.close()
        return selectedItems
    }

    fun clearAllSelectedItems() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME")
        db.close()
    }



}
