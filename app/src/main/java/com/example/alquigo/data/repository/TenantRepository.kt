package com.example.alquigo.data.repository

import com.example.alquigo.data.model.Contract
import com.example.alquigo.data.model.Payment
import com.example.alquigo.data.model.Property
import com.example.alquigo.data.model.Tenant
import com.example.alquigo.data.model.TenantRentalInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
