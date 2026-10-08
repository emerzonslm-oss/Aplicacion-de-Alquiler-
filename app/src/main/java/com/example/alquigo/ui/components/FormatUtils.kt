package com.example.alquigo.ui.components

import android.content.Context
import android.net.Uri
import java.io.File

fun formatPrecio(precio: Double): String {
    val valorEntero = precio.toInt()
    return "S/ $valorEntero"
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
