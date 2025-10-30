package com.example.myapplication

data class Empeno(
    var id: String = "",
    var producto: String = "",
    var cliente: String = "",
    var montoEmpenado: Double = 0.0,
    var precioMensual: Double = 0.0,
    var fecha: String = "",
    var userId: String = "",
    var foto1Url: String = "",
    var foto2Url: String = "",
    var estado: String = "activo",
    var fechaCreacion: com.google.firebase.Timestamp? = null
)
