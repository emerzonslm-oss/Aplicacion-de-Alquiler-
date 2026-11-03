package com.example.alquigo.data.model

data class Property(
    val id: String = "",
    val userId: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val direccion: String = "",
    val tipo: String = "",
    val imagenUri: String = "",
    val estado: String = "DISPONIBLE" // "DISPONIBLE", "OCUPADA"
)

data class DuePaymentInfo(
    val payment: Payment = Payment(),
    val propertyTitle: String = "",
    val tenantName: String = "",
    val tenantPhone: String = "",
    val isOverdue: Boolean = false,
    val daysRemaining: Long = 0
)
