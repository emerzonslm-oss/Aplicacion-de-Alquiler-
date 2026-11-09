package com.example.alquigo.data.model

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val fecha: String = "",
    val leida: Boolean = false,
    val tipo: String = "GENERAL",
    val relatedId: String = ""
)
