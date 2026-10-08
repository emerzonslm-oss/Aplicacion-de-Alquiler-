package com.example.alquigo.data.repository

import com.example.alquigo.data.model.Property
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PropertyRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("propiedades")
    private val contractsCollection = firestore.collection("contratos")

    suspend fun saveProperty(property: Property): Result<Unit> {
        return try {
            val docRef = collection.document()
            val propertyWithId = property.copy(id = docRef.id)
            docRef.set(propertyWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllProperties(): Result<List<Property>> {
        return try {
            val snapshot = collection.get().await()
            val properties = snapshot.toObjects(Property::class.java)
            Result.success(properties)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPropertiesByType(tipo: String): Result<List<Property>> {
        return try {
            val snapshot = collection.whereEqualTo("tipo", tipo).get().await()
            val properties = snapshot.toObjects(Property::class.java)
            Result.success(properties)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPropertyById(id: String): Result<Property?> {
        return try {
            val snapshot = collection.document(id).get().await()
            val property = snapshot.toObject(Property::class.java)
            Result.success(property)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProperty(property: Property): Result<Unit> {
        return try {
            collection.document(property.id).set(property).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProperty(propertyId: String): Result<Unit> {
        return try {
            collection.document(propertyId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasActiveContract(propertyId: String): Result<Boolean> {
        return try {
            // Consulta en Firestore si hay algún contrato con propertyId o idPropiedad y estado ACTIVO
            val queryPropertyId = contractsCollection
                .whereEqualTo("propertyId", propertyId)
                .whereEqualTo("estado", "ACTIVO")
                .get()
                .await()

            if (!queryPropertyId.isEmpty) {
                return Result.success(true)
            }

            val queryIdPropiedad = contractsCollection
                .whereEqualTo("idPropiedad", propertyId)
                .whereEqualTo("estado", "ACTIVO")
                .get()
                .await()

            Result.success(!queryIdPropiedad.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
