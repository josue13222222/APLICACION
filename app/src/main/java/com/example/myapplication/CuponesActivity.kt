package com.example.myapplication

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
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

                val editText = EditText(this)
                editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
                editText.hint = "Ingresa cantidad de puntos"

                android.app.AlertDialog.Builder(this)
                    .setTitle("Canjear Puntos")
                    .setMessage("Tienes $puntosTotales puntos disponibles\nCada punto = S/. 0.50\n\nIngresa la cantidad de puntos a canjear:")
                    .setView(editText)
                    .setPositiveButton("CANJEAR") { _, _ ->
                        val puntosIngresados = editText.text.toString().toIntOrNull() ?: 0

                        when {
                            puntosIngresados <= 0 -> {
                                android.app.AlertDialog.Builder(this)
                                    .setTitle("Cantidad Inválida")
                                    .setMessage("Ingresa una cantidad válida de puntos")
                                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                    .show()
                            }
                            puntosIngresados > puntosTotales -> {
                                android.app.AlertDialog.Builder(this)
                                    .setTitle("Puntos Insuficientes")
                                    .setMessage("No tienes suficientes puntos. Máximo: $puntosTotales")
                                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                    .show()
                            }
                            else -> {
                                val descuentoSoles = puntosIngresados * 0.50
                                android.app.AlertDialog.Builder(this)
                                    .setTitle("Confirmar Canje")
                                    .setMessage("Vas a canjear $puntosIngresados puntos\nDescuento: S/. ${"%.2f".format(descuentoSoles)}")
                                    .setPositiveButton("CONFIRMAR") { _, _ ->
                                        realizarCanjeoPuntos(uid, puntosIngresados)
                                    }
                                    .setNegativeButton("CANCELAR") { dialog, _ -> dialog.dismiss() }
                                    .show()
                            }
                        }
                    }
                    .setNegativeButton("CANCELAR") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
    }

    private fun realizarCanjeoPuntos(uid: String, puntosCanjear: Int) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                val puntoActuales = document.getLong("puntos")?.toInt() ?: 0
                val nuevasCantidad = puntoActuales - puntosCanjear
                val descuentoSoles = puntosCanjear * 0.50

                db.collection("usuarios").document(uid)
                    .update("puntos", nuevasCantidad)
                    .addOnSuccessListener {
                        val historial = hashMapOf(
                            "tipo" to "Canje",
                            "descripcion" to "Canje de $puntosCanjear puntos - S/. ${"%.2f".format(descuentoSoles)} de descuento",
                            "puntos" to -puntosCanjear,
                            "fecha" to System.currentTimeMillis()
                        )

                        db.collection("usuarios").document(uid)
                            .collection("historial")
                            .add(historial)
                            .addOnSuccessListener {
                                Log.d("[v0] CuponesActivity", "Canje exitoso: $puntosCanjear puntos")

                                // Save notification when points are redeemed
                                guardarNotificacionCanje(uid, puntosCanjear, descuentoSoles)

                                cargarPuntosUsuario()
                                cargarHistorialPuntos()

                                android.app.AlertDialog.Builder(this)
                                    .setTitle("¡Canje Exitoso!")
                                    .setMessage("¡Felicidades! Ganaste S/. ${"%.2f".format(descuentoSoles)} de descuento")
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

    // New function to save redemption notification
    private fun guardarNotificacionCanje(uid: String, puntosCanjear: Int, descuentoSoles: Double) {
        val notificacion = hashMapOf(
            "userId" to uid,
            "titulo" to "¡Canje de Puntos Exitoso!",
            "descripcion" to "¡Felicidades! Canjeaste $puntosCanjear puntos y ganaste S/. ${"%.2f".format(descuentoSoles)} de descuento",
            "tipo" to "canje",
            "fecha" to java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date()),
            "fechaCreacion" to com.google.firebase.Timestamp.now(),
            "leida" to false
        )

        db.collection("notificaciones")
            .add(notificacion)
            .addOnSuccessListener {
                Log.d("[v0] CuponesActivity", "Notificación de canje guardada")
            }
            .addOnFailureListener { e ->
                Log.e("[v0] CuponesActivity", "Error al guardar notificación: ${e.message}")
            }
    }
}

data class HistorialPunto(
    val tipo: String = "",
    val descripcion: String = "",
    val puntos: Int = 0,
    val fecha: Long = 0
)
