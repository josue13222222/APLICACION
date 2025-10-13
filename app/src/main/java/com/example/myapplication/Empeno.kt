package com.example.myapplication

data class Empeno(
    var id: String = "",
    var producto: String = "",
    var estado: String = "pendiente", // Agregado estado por defecto "pendiente"
    var valor: Int = 0,
    var puntos: Int = 0,
    var fecha: String = "",
    var userId: String = "",
    var foto1Url: String = "", // URL de la primera foto
    var foto2Url: String = "", // URL de la segunda foto (opcional)
    var estadoAprobacion: String = "pendiente", // pendiente, aprobado, rechazado
    var fechaCreacion: com.google.firebase.Timestamp? = null // Timestamp para ordenar
)
