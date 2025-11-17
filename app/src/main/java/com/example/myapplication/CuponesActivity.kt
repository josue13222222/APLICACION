package com.example.myapplication

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CuponesActivity : AppCompatActivity() {

    private lateinit var tvPuntos: TextView
    private lateinit var tvHistorial: TextView
    private lateinit var recyclerHistorial: RecyclerView
    private lateinit var btnCanjearPuntos: Button
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var sharedPreferences: SharedPreferences
    private val historialList = mutableListOf<HistorialPunto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cupones)

        tvPuntos = findViewById(R.id.tvPuntos)
        tvHistorial = findViewById(R.id.tvHistorial)
        recyclerHistorial = findViewById(R.id.rvCupones)
        btnCanjearPuntos = findViewById(R.id.btnCanjearPuntos)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        recyclerHistorial.layoutManager = LinearLayoutManager(this)

        cargarPuntosUsuario()
        cargarHistorialPuntos()

        btnCanjearPuntos.setOnClickListener {
            mostrarDialogoCanjearPuntos()
        }
    }

    override fun onResume() {
        super.onResume()
        cargarPuntosUsuario()
    }

    private fun cargarPuntosUsuario() {
        val uid = auth.currentUser?.uid

        if (uid.isNullOrEmpty()) {
            Log.e("[v0] CuponesActivity", "UID no disponible")
            tvPuntos.text = "Puntos disponibles: 0 (S/. 0.00)"
            return
        }

        db.collection("usuarios").document(uid)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("[v0] CuponesActivity", "Error al cargar puntos: ${error.message}")
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val puntos = document.getLong("puntos")?.toInt() ?: 0
                    val valorSoles = puntos * 0.50
                    tvPuntos.text = "Puntos disponibles: $puntos (S/. ${"%.2f".format(valorSoles)})"
                    Log.d("[v0] CuponesActivity", "Puntos cargados: $puntos para UID: $uid")
                } else {
                    tvPuntos.text = "Puntos disponibles: 0 (S/. 0.00)"
                    Log.d("[v0] CuponesActivity", "Usuario no encontrado")
                }
            }
    }

    private fun cargarHistorialPuntos() {
        val uid = auth.currentUser?.uid

        if (uid.isNullOrEmpty()) {
            Log.e("[v0] CuponesActivity", "UID no disponible para historial")
            return
        }

        db.collection("usuarios").document(uid)
            .collection("historial")
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("[v0] CuponesActivity", "Error al cargar historial: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    historialList.clear()
                    for (doc in snapshots) {
                        val transaccion = doc.toObject(HistorialPunto::class.java)
                        historialList.add(transaccion)
                    }
                    actualizarRecyclerView()
                    Log.d("[v0] CuponesActivity", "Historial cargado: ${historialList.size} registros")
                }
            }
    }

    private fun actualizarRecyclerView() {
        recyclerHistorial.adapter = HistorialPuntosAdapter(historialList)
    }

    private fun mostrarDialogoCanjearPuntos() {
        val uid = auth.currentUser?.uid

        if (uid.isNullOrEmpty()) {
            Log.e("[v0] CuponesActivity", "UID no disponible para canjear")
            return
        }

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                val puntosTotales = document.getLong("puntos")?.toInt() ?: 0

                if (puntosTotales == 0) {
                    android.app.AlertDialog.Builder(this)
                        .setTitle("Sin Puntos")
                        .setMessage("No tienes puntos disponibles para canjear")
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .show()
                    return@addOnSuccessListener
                }

                val opciones = arrayOf(
                    "5 puntos (S/. 2.50)",
                    "10 puntos (S/. 5.00)",
                    "20 puntos (S/. 10.00)",
                    "50 puntos (S/. 25.00)"
                )

                android.app.AlertDialog.Builder(this)
                    .setTitle("Canjear Puntos")
                    .setMessage("Selecciona cuántos puntos deseas canjear")
                    .setItems(opciones) { _, which ->
                        val puntosCanjear = when (which) {
                            0 -> 5
                            1 -> 10
                            2 -> 20
                            3 -> 50
                            else -> 0
                        }

                        if (puntosCanjear > 0 && puntosCanjear <= puntosTotales) {
                            realizarCanjeoPuntos(uid, puntosCanjear)
                        } else {
                            android.app.AlertDialog.Builder(this)
                                .setTitle("Puntos Insuficientes")
                                .setMessage("No tienes suficientes puntos para esta operación")
                                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                .show()
                        }
                    }
                    .show()
            }
    }

    private fun realizarCanjeoPuntos(uid: String, puntosCanjear: Int) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                val puntoActuales = document.getLong("puntos")?.toInt() ?: 0
                val nuevasCantidad = puntoActuales - puntosCanjear

                // Actualizar puntos
                db.collection("usuarios").document(uid)
                    .update("puntos", nuevasCantidad)
                    .addOnSuccessListener {
                        // Registrar en historial
                        val historial = hashMapOf(
                            "tipo" to "Canje",
                            "descripcion" to "Canje de $puntosCanjear puntos - Efectivo",
                            "puntos" to -puntosCanjear,
                            "fecha" to System.currentTimeMillis()
                        )

                        db.collection("usuarios").document(uid)
                            .collection("historial")
                            .add(historial)
                            .addOnSuccessListener {
                                Log.d("[v0] CuponesActivity", "Canje exitoso: $puntosCanjear puntos")
                                cargarPuntosUsuario()
                                cargarHistorialPuntos()

                                android.app.AlertDialog.Builder(this)
                                    .setTitle("Canje Exitoso")
                                    .setMessage("Canjeaste $puntosCanjear puntos por S/. ${"%.2f".format(puntosCanjear * 0.50)}")
                                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                    .show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("[v0] CuponesActivity", "Error al registrar canje: ${e.message}")
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("[v0] CuponesActivity", "Error al actualizar puntos: ${e.message}")
                    }
            }
    }
}

data class HistorialPunto(
    val tipo: String = "",
    val descripcion: String = "",
    val puntos: Int = 0,
    val fecha: Long = 0
)
