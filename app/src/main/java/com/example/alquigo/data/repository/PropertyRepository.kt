package com.example.alquigo.data.repository

import com.example.alquigo.data.model.Contract
import com.example.alquigo.data.model.ContractHistoryInfo
import com.example.alquigo.data.model.DuePaymentInfo
import com.example.alquigo.data.model.Payment
import com.example.alquigo.data.model.Property
import com.example.alquigo.ui.components.parseFlexibleDate
import com.google.firebase.auth.FirebaseAuth
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

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    suspend fun saveProperty(property: Property): Result<Unit> {
        return try {
            val docRef = collection.document()
            val ownerUid = property.userId.ifBlank { currentUserId }
            val propertyWithId = property.copy(
                id = docRef.id,
                userId = ownerUid,
                estado = if (property.estado.isBlank()) "DISPONIBLE" else property.estado
            )
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
            val filtered = properties.filter { it.userId.isBlank() || it.userId == currentUserId }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPropertiesByType(tipo: String): Result<List<Property>> {
        return try {
            val snapshot = collection.whereEqualTo("tipo", tipo).get().await()
            val properties = snapshot.toObjects(Property::class.java)
            val filtered = properties.filter { it.userId.isBlank() || it.userId == currentUserId }
            Result.success(filtered)
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

    suspend fun markPaymentAsPaid(paymentId: String, fechaPagoReal: String, comprobanteUrl: String = ""): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "estado" to "PAGADO",
                "fechaPagoReal" to fechaPagoReal
            )
            if (comprobanteUrl.isNotBlank()) {
                updates["comprobanteUrl"] = comprobanteUrl
            }
            paymentsCollection.document(paymentId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getContractHistoryForProperty(propertyId: String): Result<List<ContractHistoryInfo>> {
        return try {
            val snapshot = contractsCollection
                .whereEqualTo("propertyId", propertyId)
                .get()
                .await()
            val contracts = snapshot.toObjects(Contract::class.java)

            val historyList = mutableListOf<ContractHistoryInfo>()
            for (contract in contracts) {
                var tenantName = "Inquilino"
                var tenantPhone = ""
                var tenantEmail = ""
                if (contract.tenantId.isNotBlank()) {
                    try {
                        val tenantDoc = firestore.collection("inquilinos").document(contract.tenantId).get().await()
                        tenantName = tenantDoc.getString("nombre") ?: "Inquilino"
                        tenantPhone = tenantDoc.getString("telefono") ?: ""
                        tenantEmail = tenantDoc.getString("email") ?: ""
                    } catch (_: Exception) {}
                }
                historyList.add(
                    ContractHistoryInfo(
                        contract = contract,
                        tenantName = tenantName,
                        tenantPhone = tenantPhone,
                        tenantEmail = tenantEmail
                    )
                )
            }
            historyList.sortByDescending { it.contract.fechaFin }
            Result.success(historyList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renewContract(
        contractId: String,
        nuevaFechaFin: String,
        nuevoMontoMensual: Double? = null
    ): Result<Unit> {
        return try {
            val contractDoc = contractsCollection.document(contractId).get().await()
            val currentContract = contractDoc.toObject(Contract::class.java)
                ?: return Result.failure(Exception("Contrato no encontrado"))

            val oldFechaFinStr = currentContract.fechaFin
            val montoMensual = nuevoMontoMensual ?: currentContract.montoMensual

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

            val oldEnd = parseDate(oldFechaFinStr)
            val nextStart = oldEnd.plusMonths(1)
            val nextStartStr = nextStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            val batch = firestore.batch()

            val contractRef = contractsCollection.document(contractId)
            batch.update(contractRef, mapOf(
                "fechaFin" to nuevaFechaFin,
                "montoMensual" to montoMensual
            ))

            val newPayments = generateMonthlyPayments(
                contractId = contractId,
                propertyId = currentContract.propertyId,
                montoMensual = montoMensual,
                fechaInicioStr = nextStartStr,
                fechaFinStr = nuevaFechaFin
            )

            for (payment in newPayments) {
                val paymentRef = paymentsCollection.document()
                val paymentWithId = payment.copy(id = paymentRef.id)
                batch.set(paymentRef, paymentWithId)
            }

            batch.commit().await()
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

    suspend fun getDueAndOverduePaymentsForOwner(): Result<List<DuePaymentInfo>> {
        return try {
            val propertiesResult = getAllProperties()
            val properties = propertiesResult.getOrNull() ?: emptyList()
            val propertyMap = properties.associateBy { it.id }

            if (properties.isEmpty()) {
                return Result.success(emptyList())
            }

            val allPaymentsSnapshot = paymentsCollection
                .whereEqualTo("estado", "PENDIENTE")
                .get()
                .await()
            val pendingPayments = allPaymentsSnapshot.toObjects(Payment::class.java)
                .filter { propertyMap.containsKey(it.propertyId) }

            val today = LocalDate.now()
            val limit7Days = today.plusDays(7)
            val dueList = mutableListOf<DuePaymentInfo>()

            for (payment in pendingPayments) {
                val paymentDate = parseFlexibleDate(payment.fecha) ?: continue
                val isOverdue = paymentDate.isBefore(today)
                val isDueIn7Days = !paymentDate.isBefore(today) && !paymentDate.isAfter(limit7Days)

                if (isOverdue || isDueIn7Days) {
                    var tenantName = "Inquilino"
                    var tenantPhone = ""

                    if (payment.contractId.isNotBlank()) {
                        try {
                            val contractDoc = contractsCollection.document(payment.contractId).get().await()
                            val tenantId = contractDoc.getString("tenantId") ?: ""
                            if (tenantId.isNotBlank()) {
                                val tenantDoc = firestore.collection("inquilinos").document(tenantId).get().await()
                                tenantName = tenantDoc.getString("nombre") ?: "Inquilino"
                                tenantPhone = tenantDoc.getString("telefono") ?: ""
                            }
                        } catch (_: Exception) {}
                    }

                    val prop = propertyMap[payment.propertyId]
                    val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(today, paymentDate)

                    dueList.add(
                        DuePaymentInfo(
                            payment = payment,
                            propertyTitle = prop?.titulo ?: "Propiedad",
                            tenantName = tenantName,
                            tenantPhone = tenantPhone,
                            isOverdue = isOverdue,
                            daysRemaining = daysDiff
                        )
                    )
                }
            }

            dueList.sortBy { it.payment.fecha }
            Result.success(dueList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMonthlyEarningsForOwner(): Result<Double> {
        return try {
            val propertiesResult = getAllProperties()
            val properties = propertiesResult.getOrNull() ?: emptyList()
            val ownerPropIds = properties.map { it.id }.toSet()

            if (ownerPropIds.isEmpty()) return Result.success(0.0)

            val snapshot = paymentsCollection
                .whereEqualTo("estado", "PAGADO")
                .get()
                .await()
            val paidPayments = snapshot.toObjects(Payment::class.java)
                .filter { ownerPropIds.contains(it.propertyId) }

            val now = LocalDate.now()
            val currentYear = now.year
            val currentMonth = now.monthValue

            val monthlyTotal = paidPayments.sumOf { payment ->
                val realDate = parseFlexibleDate(payment.fechaPagoReal) ?: parseFlexibleDate(payment.fecha)
                if (realDate != null && realDate.year == currentYear && realDate.monthValue == currentMonth) {
                    payment.monto
                } else {
                    0.0
                }
            }

            Result.success(monthlyTotal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
