package com.example.myapplication

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore

object SistemaPuntos {
    private val db = FirebaseFirestore.getInstance()

    fun cargarPuntosDelUsuario(context: Context, telefonoUsuario: String, callback: (Int) -> Unit) {
        db.collection("usuarios")
            .whereEqualTo("telefono", telefonoUsuario)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    callback(0)
                    return@addSnapshotListener
                }

                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    val puntos = querySnapshot.documents[0].getLong("puntos")?.toInt() ?: 0
                    callback(puntos)
                } else {
                    callback(0)
                }
            }
    }

    fun agregarPuntosFirestore(telefonoUsuario: String, monto: Double) {
        val puntosGanados = (monto / 100).toInt()

        if (puntosGanados > 0 && telefonoUsuario.isNotEmpty()) {
            db.collection("usuarios")
                .whereEqualTo("telefono", telefonoUsuario)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.documents.isNotEmpty()) {
                        val usuarioId = querySnapshot.documents[0].id
                        db.collection("usuarios").document(usuarioId)
                            .update("puntos", com.google.firebase.firestore.FieldValue.increment(puntosGanados.toLong()))
                            .addOnSuccessListener {
                                android.util.Log.d("[v0]", "Puntos agregados correctamente: $puntosGanados para $telefonoUsuario")
                            }
                            .addOnFailureListener { e ->
                                android.util.Log.e("[v0]", "Error al agregar puntos: ${e.message}")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("[v0]", "Error al buscar usuario: ${e.message}")
                }
        }
    }

    fun obtenerPuntosSync(context: Context): Int {
        return 0
    }
}
