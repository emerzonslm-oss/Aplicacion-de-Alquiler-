package com.example.alquigo.data.repository

import com.example.alquigo.data.model.Contract
import com.example.alquigo.data.model.Payment
import com.example.alquigo.data.model.Property
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class PropertyRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("propiedades")
    private val contractsCollection = firestore.collection("contratos")
    private val paymentsCollection = firestore.collection("pagos")

    suspend fun saveProperty(property: Property): Result<Unit> {
        return try {
            val docRef = collection.document()
            val propertyWithId = property.copy(id = docRef.id, estado = if (property.estado.isBlank()) "DISPONIBLE" else property.estado)
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

    suspend fun getActiveContractForProperty(propertyId: String): Result<Contract?> {
        return try {
            val snapshot = contractsCollection
                .whereEqualTo("propertyId", propertyId)
                .whereEqualTo("estado", "ACTIVO")
                .get()
                .await()
            if (!snapshot.isEmpty) {
                Result.success(snapshot.documents[0].toObject(Contract::class.java))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentPaymentsForContract(contractId: String): Result<List<Payment>> {
        return try {
            val snapshot = paymentsCollection
                .whereEqualTo("contractId", contractId)
                .orderBy("fecha", Query.Direction.ASCENDING)
                .get()
                .await()
            Result.success(snapshot.toObjects(Payment::class.java))
        } catch (e: Exception) {
            try {
                val snapshot = paymentsCollection
                    .whereEqualTo("contractId", contractId)
                    .get()
                    .await()
                val list = snapshot.toObjects(Payment::class.java).sortedBy { it.fecha }
                Result.success(list)
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        }
    }

    suspend fun getAllPaymentsForProperty(propertyId: String): Result<List<Payment>> {
        return try {
            val snapshot = paymentsCollection
                .whereEqualTo("propertyId", propertyId)
                .get()
                .await()
            val list = snapshot.toObjects(Payment::class.java).sortedBy { it.fecha }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markPaymentAsPaid(paymentId: String, fechaPagoReal: String): Result<Unit> {
        return try {
            paymentsCollection.document(paymentId).update(
                mapOf(
                    "estado" to "PAGADO",
                    "fechaPagoReal" to fechaPagoReal
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createContractAndAssignTenant(
        propertyId: String,
        tenantId: String,
        fechaInicio: String,
        fechaFin: String,
        montoMensual: Double
    ): Result<Unit> {
        return try {
            val batch = firestore.batch()

            val contractRef = contractsCollection.document()
            val contractId = contractRef.id

            val newContract = Contract(
                id = contractId,
                propertyId = propertyId,
                tenantId = tenantId,
                fechaInicio = fechaInicio,
                fechaFin = fechaFin,
                montoMensual = montoMensual,
                estado = "ACTIVO"
            )
            batch.set(contractRef, newContract)

            val payments = generateMonthlyPayments(
                contractId = contractId,
                propertyId = propertyId,
                montoMensual = montoMensual,
                fechaInicioStr = fechaInicio,
                fechaFinStr = fechaFin
            )

            for (payment in payments) {
                val paymentRef = paymentsCollection.document()
                val paymentWithId = payment.copy(id = paymentRef.id)
                batch.set(paymentRef, paymentWithId)
            }

            val propertyRef = collection.document(propertyId)
            batch.update(propertyRef, "estado", "OCUPADA")

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateMonthlyPayments(
        contractId: String,
        propertyId: String,
        montoMensual: Double,
        fechaInicioStr: String,
        fechaFinStr: String
    ): List<Payment> {
        val payments = mutableListOf<Payment>()
        val formatters = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
        )

        fun parseDate(str: String): LocalDate {
            for (fmt in formatters) {
                try {
                    return LocalDate.parse(str.trim(), fmt)
                } catch (_: Exception) {}
            }
            return LocalDate.now()
        }

        val start = parseDate(fechaInicioStr)
        val end = parseDate(fechaFinStr)

        var current = start
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var count = 0

        while (!current.isAfter(end) && count < 120) {
            val paymentDateStr = current.format(outputFormatter)
            val monthName = current.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-ES"))
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("es-ES")) else it.toString() }
            val year = current.year

            payments.add(
                Payment(
                    contractId = contractId,
                    propertyId = propertyId,
                    monto = montoMensual,
                    fecha = paymentDateStr,
                    estado = "PENDIENTE",
                    concepto = "Mensualidad $monthName $year"
                )
            )
            current = current.plusMonths(1)
            count++
        }

        return payments
    }
}
