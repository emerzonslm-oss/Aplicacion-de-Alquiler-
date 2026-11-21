package com.example.alquigo.data.repository

import com.example.alquigo.data.model.Contract
import com.example.alquigo.data.model.Payment
import com.example.alquigo.data.model.Property
import com.example.alquigo.data.model.Tenant
import com.example.alquigo.data.model.TenantRentalInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class TenantItemInfo(
    val tenant: Tenant = Tenant(),
    val propertyId: String = "",
    val propertyTitle: String = ""
)

class TenantRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("inquilinos")

    suspend fun getAllTenants(): Result<List<Tenant>> {
        return try {
            val snapshot = collection.get().await()
            val tenants = snapshot.toObjects(Tenant::class.java)
            Result.success(tenants)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllTenantsWithProperty(): Result<List<TenantItemInfo>> {
        return try {
            val tenantsSnapshot = collection.get().await()
            val tenants = tenantsSnapshot.toObjects(Tenant::class.java)

            val contractsSnapshot = firestore.collection("contratos")
                .whereEqualTo("estado", "ACTIVO")
                .get()
                .await()
            val activeContracts = contractsSnapshot.toObjects(Contract::class.java)

            val propertiesSnapshot = firestore.collection("propiedades").get().await()
            val propertiesMap = propertiesSnapshot.documents.associate { doc ->
                doc.id to (doc.getString("titulo") ?: "Propiedad")
            }

            val tenantInfoList = tenants.map { tenant ->
                val contract = activeContracts.find { it.tenantId == tenant.id }
                val propId = contract?.propertyId ?: ""
                val propTitle = if (propId.isNotBlank()) propertiesMap[propId] ?: "Propiedad Asignada" else ""
                TenantItemInfo(
                    tenant = tenant,
                    propertyId = propId,
                    propertyTitle = propTitle
                )
            }

            Result.success(tenantInfoList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTenantById(tenantId: String): Result<Tenant?> {
        return try {
            val snapshot = collection.document(tenantId).get().await()
            val tenant = snapshot.toObject(Tenant::class.java)
            Result.success(tenant)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkDniExists(dni: String): Result<Boolean> {
        return try {
            val snapshot = collection.whereEqualTo("dni", dni).get().await()
            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveTenant(tenant: Tenant): Result<Unit> {
        return try {
            val docRef = collection.document()
            val tenantWithId = tenant.copy(id = docRef.id)
            docRef.set(tenantWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTenantRentalInfo(userEmail: String): Result<TenantRentalInfo?> {
        return try {
            val cleanEmail = userEmail.trim()
            if (cleanEmail.isBlank()) return Result.success(null)

            val tenantSnapshot = collection.whereEqualTo("email", cleanEmail).get().await()
            var tenant: Tenant? = if (!tenantSnapshot.isEmpty) {
                tenantSnapshot.documents[0].toObject(Tenant::class.java)
            } else {
                val allTenants = collection.get().await().toObjects(Tenant::class.java)
                allTenants.find { it.email.equals(cleanEmail, ignoreCase = true) }
            }

            if (tenant == null) {
                return Result.success(null)
            }

            val contractSnapshot = firestore.collection("contratos")
                .whereEqualTo("tenantId", tenant.id)
                .whereEqualTo("estado", "ACTIVO")
                .get()
                .await()

            val contract = if (!contractSnapshot.isEmpty) {
                contractSnapshot.documents[0].toObject(Contract::class.java)
            } else {
                val allContracts = firestore.collection("contratos")
                    .whereEqualTo("tenantId", tenant.id)
                    .get()
                    .await()
                if (!allContracts.isEmpty) allContracts.documents[0].toObject(Contract::class.java) else null
            }

            var property: Property? = null
            var payments: List<Payment> = emptyList()

            if (contract != null && contract.propertyId.isNotBlank()) {
                val propertyDoc = firestore.collection("propiedades").document(contract.propertyId).get().await()
                property = propertyDoc.toObject(Property::class.java)

                val paymentsSnapshot = firestore.collection("pagos")
                    .whereEqualTo("contractId", contract.id)
                    .get()
                    .await()
                payments = paymentsSnapshot.toObjects(Payment::class.java).sortedBy { it.fecha }
            }

            Result.success(
                TenantRentalInfo(
                    property = property,
                    contract = contract,
                    payments = payments,
                    tenant = tenant
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
