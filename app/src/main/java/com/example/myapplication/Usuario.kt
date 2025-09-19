package com.example.myapplication

data class Usuario(
    var id: String = "",
    val email: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val activo: Boolean = true,
    val puntos: Int = 0,
    val fechaRegistro: com.google.firebase.Timestamp? = null,
    val isAdmin: Boolean = false
)
