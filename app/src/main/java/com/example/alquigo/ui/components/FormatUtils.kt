package com.example.alquigo.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun parseFlexibleDate(str: String): LocalDate? {
    if (str.isBlank()) return null
    val formatters = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("d/M/yyyy")
    )
    for (fmt in formatters) {
        try {
            return LocalDate.parse(str.trim(), fmt)
        } catch (_: Exception) {}
    }
    return null
}

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

fun openWhatsAppReminder(
    context: Context,
    tenantPhone: String,
    tenantName: String,
    propertyTitle: String,
    amount: Double,
    dueDateStr: String,
    isOverdue: Boolean
) {
    val cleanPhone = tenantPhone.filter { it.isDigit() }
    val formattedPhone = when {
        cleanPhone.length == 9 -> "51$cleanPhone"
        cleanPhone.length == 11 && cleanPhone.startsWith("51") -> cleanPhone
        cleanPhone.isNotBlank() -> "51$cleanPhone"
        else -> ""
    }

    if (formattedPhone.isBlank()) {
        android.widget.Toast.makeText(context, "Número de teléfono no disponible para WhatsApp.", android.widget.Toast.LENGTH_SHORT).show()
        return
    }

    val estadoTexto = if (isOverdue) "VENCIDO 🔴" else "PRÓXIMO A VENCER 🟠"
    val montoTexto = formatPrecio(amount)

    val message = """
        Hola $tenantName, te saludamos de AlquiGo. �

        Te recordamos que la mensualidad de la propiedad '$propertyTitle' por el monto de $montoTexto tiene fecha de vencimiento el $dueDateStr.

        Estado actual: $estadoTexto

        Por favor, envíanos el comprobante una vez realizado el pago. ¡Muchas gracias!
    """.trimIndent()

    try {
        val encodedMessage = java.net.URLEncoder.encode(message, "UTF-8")
        val whatsappUrl = "https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMessage"
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
        context.startActivity(intent)
    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "No se pudo abrir WhatsApp. Asegúrate de tener la aplicación instalada.",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}

fun sendReceiptEmail(
    context: Context,
    tenantEmail: String,
    tenantName: String,
    propertyTitle: String,
    montoStr: String,
    concepto: String,
    fechaPagoReal: String
) {
    val cleanEmail = tenantEmail.trim()
    if (cleanEmail.isBlank()) {
        android.widget.Toast.makeText(context, "El correo electrónico del inquilino no está registrado.", android.widget.Toast.LENGTH_LONG).show()
        return
    }

    val subject = "Comprobante de Pago Confirmado - AlquiGo ($propertyTitle)"
    val body = """
        Hola $tenantName,

        Confirmamos la recepción de tu pago correspondiente a la propiedad '$propertyTitle'.

        • Detalle: $concepto
        • Monto: $montoStr
        • Fecha de pago: $fechaPagoReal
        • Estado: PAGADO (Confirmado)

        ¡Muchas gracias por tu pago puntual!

        Atentamente,
        Administración AlquiGo
    """.trimIndent()

    try {
        val encodedSubject = java.net.URLEncoder.encode(subject, "UTF-8").replace("+", "%20")
        val encodedBody = java.net.URLEncoder.encode(body, "UTF-8").replace("+", "%20")
        val mailtoUrl = "mailto:$cleanEmail?subject=$encodedSubject&body=$encodedBody"

        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO, Uri.parse(mailtoUrl))
        context.startActivity(android.content.Intent.createChooser(intent, "Enviar confirmación por correo"))
    } catch (e: Exception) {
        try {
            val fallbackIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(cleanEmail))
                putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
                putExtra(android.content.Intent.EXTRA_TEXT, body)
            }
            context.startActivity(android.content.Intent.createChooser(fallbackIntent, "Enviar correo al inquilino"))
        } catch (_: Exception) {
            android.widget.Toast.makeText(context, "No se pudo abrir ninguna aplicación de correo.", android.widget.Toast.LENGTH_LONG).show()
        }
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
