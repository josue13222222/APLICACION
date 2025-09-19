package com.example.myapplication

data class Cupon(
    var id: String = "",
    val codigo: String = "",
    val descuentoPorcentaje: Double = 0.0,
    val descripcion: String = "",
    val activo: Boolean = true,
    val fechaCreacion: com.google.firebase.Timestamp? = null,
    val usos: Int = 0,
    // Legacy fields for backward compatibility
    val nombre: String = "",
    val tipo: String = "",
    val fechaExpiracion: String = "",
    val estado: String = ""
)
