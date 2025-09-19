package com.example.myapplication

import com.google.firebase.Timestamp

data class OrdenReparacion(
    var id: String = "",
    val userId: String = "",
    val nombreUsuario: String = "",
    val telefono: String = "",
    val dispositivo: String = "",
    val problema: String = "",
    val estado: String = "Recibido", // Recibido, En Diagnóstico, En Reparación, Listo, Entregado
    val fechaCreacion: Timestamp? = null,
    val fechaActualizacion: Timestamp? = null,
    val costoEstimado: Double = 0.0,
    val observaciones: String = "",
    val puntosGanados: Int = 0
)
