package com.example.myapplication

import android.content.Context
import android.util.Log
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

    fun agregarPuntosFirestoreUID(userId: String, monto: Double) {
        val puntosGanados = (monto / 100).toInt()

        if (puntosGanados > 0 && userId.isNotEmpty()) {
            Log.d("[v0]", "Intentando agregar $puntosGanados puntos por UID: $userId")

            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        db.collection("usuarios").document(userId)
                            .update("puntos", com.google.firebase.firestore.FieldValue.increment(puntosGanados.toLong()))
                            .addOnSuccessListener {
                                registrarTransaccionPuntos(userId, puntosGanados, "Pago recibido - S/. ${"%.2f".format(monto)}")
                                Log.d("[v0]", "Puntos agregados correctamente: $puntosGanados para UID $userId")
                            }
                            .addOnFailureListener { e ->
                                Log.e("[v0]", "Error al agregar puntos: ${e.message}")
                            }
                    } else {
                        Log.e("[v0]", "Usuario no encontrado con UID: $userId, intentando por teléfono")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("[v0]", "Error al buscar usuario por UID: ${e.message}")
                }
        }
    }

    fun agregarPuntosFirestore(telefonoUsuario: String, monto: Double) {
        val puntosGanados = (monto / 100).toInt()

        if (puntosGanados > 0 && telefonoUsuario.isNotEmpty()) {
            Log.d("[v0]", "Intentando agregar $puntosGanados puntos por teléfono: $telefonoUsuario")

            db.collection("usuarios")
                .whereEqualTo("telefono", telefonoUsuario)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.documents.isNotEmpty()) {
                        val usuarioId = querySnapshot.documents[0].id
                        db.collection("usuarios").document(usuarioId)
                            .update("puntos", com.google.firebase.firestore.FieldValue.increment(puntosGanados.toLong()))
                            .addOnSuccessListener {
                                registrarTransaccionPuntos(usuarioId, puntosGanados, "Pago recibido - S/. ${"%.2f".format(monto)}")
                                Log.d("[v0]", "Puntos agregados correctamente: $puntosGanados para $telefonoUsuario")
                            }
                            .addOnFailureListener { e ->
                                Log.e("[v0]", "Error al agregar puntos: ${e.message}")
                            }
                    } else {
                        Log.e("[v0]", "Usuario no encontrado con teléfono: $telefonoUsuario")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("[v0]", "Error al buscar usuario: ${e.message}")
                }
        }
    }

    fun agregarPuntosFirestoreEmail(emailUsuario: String, monto: Double) {
        val puntosGanados = (monto / 100).toInt()

        if (puntosGanados > 0 && emailUsuario.isNotEmpty()) {
            Log.d("[v0]", "Intentando agregar $puntosGanados puntos por email: $emailUsuario")

            db.collection("usuarios")
                .whereEqualTo("email", emailUsuario)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.documents.isNotEmpty()) {
                        val usuarioId = querySnapshot.documents[0].id
                        db.collection("usuarios").document(usuarioId)
                            .update("puntos", com.google.firebase.firestore.FieldValue.increment(puntosGanados.toLong()))
                            .addOnSuccessListener {
                                registrarTransaccionPuntos(usuarioId, puntosGanados, "Pago recibido - S/. ${"%.2f".format(monto)}")
                                Log.d("[v0]", "Puntos agregados correctamente: $puntosGanados para $emailUsuario")
                            }
                            .addOnFailureListener { e ->
                                Log.e("[v0]", "Error al agregar puntos: ${e.message}")
                            }
                    } else {
                        Log.e("[v0]", "Usuario no encontrado con email: $emailUsuario")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("[v0]", "Error al buscar usuario: ${e.message}")
                }
        }
    }

    private fun registrarTransaccionPuntos(usuarioId: String, puntos: Int, descripcion: String) {
        val transaccion = mapOf(
            "puntos" to puntos,
            "descripcion" to descripcion,
            "fecha" to System.currentTimeMillis(),
            "tipo" to "Ganancia"
        )

        db.collection("usuarios").document(usuarioId)
            .collection("historial")
            .add(transaccion)
            .addOnSuccessListener {
                Log.d("[v0]", "Transacción registrada: $descripcion - $puntos puntos")
            }
            .addOnFailureListener { e ->
                Log.e("[v0]", "Error al registrar transacción: ${e.message}")
            }
    }

    fun obtenerPuntosSync(context: Context): Int {
        return 0
    }
}
