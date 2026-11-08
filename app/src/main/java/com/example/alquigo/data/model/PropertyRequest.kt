package com.example.alquigo.data.model

data class PropertyRequest(
    val id: String = "",
    val propertyId: String = "",
    val propertyTitle: String = "",
    val propertyTipo: String = "",
    val propertyPrecio: Double = 0.0,
    val propertyImagenUri: String = "",
    val tenantUid: String = "",
    val tenantName: String = "",
    val tenantEmail: String = "",
    val tenantPhone: String = "",
    val fechaSolicitud: String = "",
    val estado: String = "PENDIENTE" // "PENDIENTE", "APROBADA", "RECHAZADA"
)
