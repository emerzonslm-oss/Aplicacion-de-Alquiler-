package com.example.alquigo.data.model

data class Contract(
    val id: String = "",
    val propertyId: String = "",
    val tenantId: String = "",
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val montoMensual: Double = 0.0,
    val estado: String = "ACTIVO" // "ACTIVO", "INACTIVO", "FINALIZADO"
)
