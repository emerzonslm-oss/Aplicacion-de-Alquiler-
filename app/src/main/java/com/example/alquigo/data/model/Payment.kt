package com.example.alquigo.data.model

data class Payment(
    val id: String = "",
    val contractId: String = "",
    val propertyId: String = "",
    val monto: Double = 0.0,
    val fecha: String = "",
    val estado: String = "PENDIENTE", // "PENDIENTE", "PAGADO"
    val concepto: String = "Mensualidad"
)
