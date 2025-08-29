package com.example.myapplication  // Este paquete debe ser igual en todos tus archivos para que se "vean" entre sí

data class Compra(
    val fecha: String,
    val numeroOrden: String,
    val total: Double,   // Aquí el total es Double para poder hacer cálculos
    val estado: String
)
