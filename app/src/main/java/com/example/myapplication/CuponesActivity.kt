package com.example.myapplication

import android.os.Bundle
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
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val historialList = mutableListOf<HistorialPunto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cupones)

        tvPuntos = findViewById(R.id.tvPuntos)
        tvHistorial = findViewById(R.id.tvHistorial)
        recyclerHistorial = findViewById(R.id.rvCupones)

        recyclerHistorial.layoutManager = LinearLayoutManager(this)

        cargarPuntosUsuario()
        cargarHistorialPuntos()
    }

    private fun cargarPuntosUsuario() {
        val userId = auth.currentUser?.uid ?: return

        var puntosEmpeños = 0
        var puntosPagos = 0

        // Calcular puntos de empeños
        db.collection("empenos")
            .whereEqualTo("userId", userId)
            .whereEqualTo("estado", "Aprobado")
            .get()
            .addOnSuccessListener { empenosSnapshot ->
                puntosEmpeños = empenosSnapshot.documents.sumOf {
                    it.getLong("puntos")?.toInt() ?: 0
                }
                actualizarTextoPuntos(puntosEmpeños, puntosPagos)
            }

        // Calcular puntos de pagos (100 soles = 1 punto)
        db.collection("pagos")
            .whereEqualTo("userId", userId)
            .whereEqualTo("estado", "Completado")
            .get()
            .addOnSuccessListener { pagosSnapshot ->
                val totalPagos = pagosSnapshot.documents.sumOf {
                    it.getDouble("monto") ?: 0.0
                }
                puntosPagos = (totalPagos / 100).toInt()
                actualizarTextoPuntos(puntosEmpeños, puntosPagos)
            }
    }

    private fun actualizarTextoPuntos(empeños: Int, pagos: Int) {
        val totalPuntos = empeños + pagos
        val valorSoles = totalPuntos * 0.50
        tvPuntos.text = "Puntos disponibles: $totalPuntos (S/. ${"%.2f".format(valorSoles)})"
    }

    private fun cargarHistorialPuntos() {
        val userId = auth.currentUser?.uid ?: return
        historialList.clear()

        // Cargar empeños aprobados
        db.collection("empenos")
            .whereEqualTo("userId", userId)
            .whereEqualTo("estado", "Aprobado")
            .get()
            .addOnSuccessListener { empenosSnapshot ->
                for (doc in empenosSnapshot.documents) {
                    val puntos = doc.getLong("puntos")?.toInt() ?: 0
                    val fecha = doc.getTimestamp("fechaCreacion")?.toDate()?.toString() ?: "Fecha desconocida"
                    val articulo = doc.getString("articulo") ?: "Empeño"

                    historialList.add(
                        HistorialPunto(
                            tipo = "Empeño",
                            descripcion = "Empeño de $articulo",
                            puntos = puntos,
                            fecha = fecha
                        )
                    )
                }
                actualizarRecyclerView()
            }

        // Cargar pagos completados
        db.collection("pagos")
            .whereEqualTo("userId", userId)
            .whereEqualTo("estado", "Completado")
            .get()
            .addOnSuccessListener { pagosSnapshot ->
                for (doc in pagosSnapshot.documents) {
                    val monto = doc.getDouble("monto") ?: 0.0
                    val puntos = (monto / 100).toInt()
                    val fecha = doc.getTimestamp("fechaPago")?.toDate()?.toString() ?: "Fecha desconocida"
                    val servicio = doc.getString("servicio") ?: "Reparación"

                    if (puntos > 0) {
                        historialList.add(
                            HistorialPunto(
                                tipo = "Reparación",
                                descripcion = "Pago de $servicio (S/. ${"%.2f".format(monto)})",
                                puntos = puntos,
                                fecha = fecha
                            )
                        )
                    }
                }
                actualizarRecyclerView()
            }
    }

    private fun actualizarRecyclerView() {
        recyclerHistorial.adapter = HistorialPuntosAdapter(historialList)
    }
}

data class HistorialPunto(
    val tipo: String,
    val descripcion: String,
    val puntos: Int,
    val fecha: String
)
