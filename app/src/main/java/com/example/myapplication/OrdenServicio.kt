package com.example.myapplication

data class OrdenServicio(
    var id: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val equipo: String = "",
    val problema: String = "",
    val estado: String = "Pendiente",
    val timestamp: Long = 0L,
    val imagenes: List<String> = emptyList(),
    val userId: String = "",
    val ordenId: String = ""
)
