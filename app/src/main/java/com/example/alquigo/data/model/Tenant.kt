package com.example.alquigo.data.model

data class Tenant(
    val id: String = "",
    val nombre: String = "",
    val dni: String = "",
    val telefono: String = "",
    val email: String = ""
)

data class TenantRentalInfo(
    val property: Property? = null,
    val contract: Contract? = null,
    val payments: List<Payment> = emptyList(),
    val tenant: Tenant? = null
)
