package com.example.alquigo.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File

fun formatPrecio(precio: Double): String {
    val valorEntero = precio.toInt()
    return "S/ $valorEntero"
}

fun getImageModel(imageUriStr: String): Any {
    if (imageUriStr.isBlank()) return ""

    if (imageUriStr.startsWith("data:image") || imageUriStr.contains(";base64,")) {
        return try {
            val base64Data = imageUriStr.substringAfter(";base64,")
            Base64.decode(base64Data, Base64.DEFAULT)
        } catch (e: Exception) {
            imageUriStr
        }
    }

    return imageUriStr
}

fun compressAndConvertUriToBase64(context: Context, uri: Uri): String {
    return try {
        val uriStr = uri.toString()
        if (uriStr.startsWith("data:image") || uriStr.startsWith("http")) {
            return uriStr
        }

        val inputStream = context.contentResolver.openInputStream(uri) ?: return copyUriToInternalStorage(context, uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return copyUriToInternalStorage(context, uri)

        val maxDimension = 600
        val width = originalBitmap.width
        val height = originalBitmap.height
        val scale = if (width > height) maxDimension.toFloat() / width else maxDimension.toFloat() / height
        
        val scaledWidth = (width * scale).toInt().coerceAtLeast(1)
        val scaledHeight = (height * scale).toInt().coerceAtLeast(1)

        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true)

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        val imageBytes = outputStream.toByteArray()

        val base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        "data:image/jpeg;base64,$base64String"
    } catch (e: Exception) {
        copyUriToInternalStorage(context, uri)
    }
}

fun copyUriToInternalStorage(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return uri.toString()
        val dir = File(context.filesDir, "properties_images")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "property_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        Uri.fromFile(file).toString()
    } catch (e: Exception) {
        uri.toString()
    }
}
