package com.example.project_pemob_techie.ui.content

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

object ImageCacheHelper {

    fun saveImageToFile(context: Context, hexString: String, fileName: String): String? {
        val bytes = hexStringToByteArray(hexString)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        bitmap?.let {
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
            }
            return file.absolutePath
        }
        return null
    }

    private fun hexStringToByteArray(hexString: String): ByteArray {
        val cleanHexString = if (hexString.startsWith("0x")) hexString.substring(2) else hexString
        val result = ByteArray(cleanHexString.length / 2)
        for (i in cleanHexString.indices step 2) {
            result[i / 2] = cleanHexString.substring(i, i + 2).toInt(16).toByte()
        }
        return result
    }
}
