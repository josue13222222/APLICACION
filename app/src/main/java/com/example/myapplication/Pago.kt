package com.example.myapplication

import com.google.firebase.Timestamp

data class Pago(
    val id: String = "",
    val userId: String = "",
    val servicioTipo: String = "",
    val monto: Double = 0.0,
    val metodoPago: String = "",
    val numeroReferencia: String = "",
    val estado: String = "Pendiente",
    val fecha: Timestamp = Timestamp.now(),
    val descripcion: String = "",
    val numeroContacto: String = "975167294",
    val cuentaBCP: String = "36592395059088"
)
