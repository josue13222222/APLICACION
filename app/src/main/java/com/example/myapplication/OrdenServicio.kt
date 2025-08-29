package com.example.myapplication

data class OrdenServicio(
    var id: String = "",   // id del documento Firestore
    val nombre: String = "",
    val telefono: String = "",
    val equipo: String = "",
    val problema: String = "",
    val estado: String = "Pendiente",
    val timestamp: Long = 0L
)
