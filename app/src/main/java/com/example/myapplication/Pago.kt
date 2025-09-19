package com.example.myapplication

import com.google.firebase.Timestamp

data class Pago(
    val id: String = "",
    val userId: String = "",
    val servicioTipo: String = "", // "Robot Rescate" o "Robot Empeño"
    val monto: Double = 0.0,
    val metodoPago: String = "", // "Yape", "Plin", "BCP"
    val numeroReferencia: String = "",
    val estado: String = "Pendiente", // "Pendiente", "Confirmado", "Rechazado"
    val fecha: Timestamp = Timestamp.now(),
    val descripcion: String = "",
    val numeroContacto: String = "93419837", // Para Yape/Plin
    val cuentaBCP: String = "191-123456789-0-12" // Cuenta BCP ejemplo
)
