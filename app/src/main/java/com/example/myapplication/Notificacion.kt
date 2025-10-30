package com.example.myapplication

data class Notificacion(
    var id: String = "",
    var titulo: String = "",
    var descripcion: String = "",
    var fecha: String = "",
    var userId: String = "",
    var tipo: String = "", // "reparacion", "empeno"
    var referenceId: String = "", // ID de la reparación o empeño
    var leida: Boolean = false,
    var fechaCreacion: com.google.firebase.Timestamp? = null
)
