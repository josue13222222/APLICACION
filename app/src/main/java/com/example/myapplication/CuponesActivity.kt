package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CuponesActivity : AppCompatActivity() {

    private lateinit var tvPuntos: TextView
    private lateinit var btnCanjear: Button
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cupones)

        tvPuntos = findViewById(R.id.tvPuntos)
        btnCanjear = findViewById(R.id.btnCanjear)

        cargarPuntosUsuario()

        val cupones = listOf(
            Cupon("", "DESCUENTO10", 10.0, "Descuento 10% en servicios", true, null, 0, "", "", "31/12/2024", ""),
            Cupon("", "ENVIOGRATIS", 0.0, "Envío Gratis", true, null, 0, "", "", "30/06/2025", ""),
            Cupon("", "REPARACION20", 20.0, "20% descuento en reparaciones", true, null, 0, "", "", "31/03/2025", "")
        )

        val recyclerView: RecyclerView = findViewById(R.id.rvCupones)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CuponesAdapter(this, cupones)

        val filtros = listOf("Todos", "Descuento", "Envío Gratuito", "Reparaciones")
        val spinner = findViewById<Spinner>(R.id.spinnerFiltro)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filtros)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        btnCanjear.setOnClickListener {
            canjearPuntos()
        }
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

    private fun canjearPuntos() {
        Toast.makeText(this, "🎫 Función de canje próximamente disponible", Toast.LENGTH_SHORT).show()
    }
}
