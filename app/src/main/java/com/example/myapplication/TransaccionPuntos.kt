package com.example.myapplication

data class TransaccionPuntos(
    val tipo: String = "",
    val puntos: Long = 0,
    val fecha: com.google.firebase.Timestamp? = null,
    val descripcion: String = ""
)
