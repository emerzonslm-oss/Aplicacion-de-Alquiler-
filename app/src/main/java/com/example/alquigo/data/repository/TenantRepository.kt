package com.example.alquigo.data.repository

import com.example.alquigo.data.model.Tenant
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
}
