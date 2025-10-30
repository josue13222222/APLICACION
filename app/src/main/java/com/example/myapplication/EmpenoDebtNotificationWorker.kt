package com.example.myapplication

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class EmpenoDebtNotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val db = Firebase.firestore

    override fun doWork(): Result {
        return try {
            Log.d("[v0]", "EmpenoDebtNotificationWorker ejecutándose")
            verificarEmpenosYCrearNotificaciones()
            Result.success()
        } catch (e: Exception) {
            Log.e("[v0]", "Error en EmpenoDebtNotificationWorker: ${e.message}")
            Result.retry()
        }
    }

    private fun verificarEmpenosYCrearNotificaciones() {
        db.collection("empenos")
            .whereEqualTo("estado", "activo")
            .get()
            .addOnSuccessListener { documents ->
                val ahora = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                for (document in documents) {
                    val empeno = document.toObject(Empeno::class.java)
                    val fechaEmpenoStr = empeno.fecha
                    val fechaEmpeno = dateFormat.parse(fechaEmpenoStr) ?: continue
                    val calEmpeno = Calendar.getInstance().apply { time = fechaEmpeno }

                    // Calcular días desde que se empeñó
                    val diasTranscurridos = ((ahora.timeInMillis - calEmpeno.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

                    // Crear notificación cada 7 días (semanal)
                    if (diasTranscurridos > 0 && diasTranscurridos % 7 == 0) {
                        crearNotificacionDeuda(empeno, diasTranscurridos)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("[v0]", "Error verificando empeños: ${e.message}")
            }
    }

    private fun crearNotificacionDeuda(empeno: Empeno, diasTranscurridos: Int) {
        val semanas = diasTranscurridos / 7
        val deudaTotal = empeno.precioMensual * (semanas / 4.3) // Aproximado a meses

        val notificacion = hashMapOf(
            "titulo" to "Recordatorio de Deuda - Empeño",
            "descripcion" to "Tu empeño de ${empeno.producto} tiene una deuda de S/ ${String.format("%.2f", deudaTotal)}. Lleva ${diasTranscurridos} días en la tienda.",
            "fecha" to SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
            "userId" to empeno.cliente,
            "tipo" to "empeno",
            "referenceId" to empeno.id,
            "leida" to false,
            "fechaCreacion" to com.google.firebase.Timestamp.now()
        )

        db.collection("notificaciones").document()
            .set(notificacion)
            .addOnSuccessListener {
                Log.d("[v0]", "Notificación de deuda creada para empeño: ${empeno.id}")
            }
            .addOnFailureListener { e ->
                Log.e("[v0]", "Error creando notificación de deuda: ${e.message}")
            }
    }
}
