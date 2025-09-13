package com.example.myapplication
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MisPuntosActivity : AppCompatActivity() {

    private lateinit var tvPuntosEmpeño: TextView
    private lateinit var tvPuntosReparacion: TextView
    private lateinit var tvTotalPuntos: TextView
    private lateinit var btnCanjear: Button
    private lateinit var recyclerHistorial: RecyclerView

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    private val historialList = mutableListOf<TransaccionPuntos>()
    private lateinit var adapter: HistorialAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_puntos)

        tvPuntosEmpeño = findViewById(R.id.tvPuntosEmpeño)
        tvPuntosReparacion = findViewById(R.id.tvPuntosReparacion)
        tvTotalPuntos = findViewById(R.id.tvTotalPuntos)
        btnCanjear = findViewById(R.id.btnCanjear)
        recyclerHistorial = findViewById(R.id.recyclerHistorial)

        recyclerHistorial.layoutManager = LinearLayoutManager(this)
        adapter = HistorialAdapter(historialList)
        recyclerHistorial.adapter = adapter

        cargarPuntos()
        cargarHistorial()

        btnCanjear.setOnClickListener {
            // Abre una nueva Activity para canjear puntos
            val intent = Intent(this, MisPuntosActivity::class.java)
            startActivity(intent)
        }
    }

    private fun cargarPuntos() {
        uid?.let {
            db.collection("usuarios_puntos").document(it)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val puntosEmpeño = doc.getLong("puntosEmpeño") ?: 0
                        val puntosReparacion = doc.getLong("puntosReparacion") ?: 0
                        val totalPuntos = doc.getLong("totalPuntos") ?: 0

                        tvPuntosEmpeño.text = "Puntos Empeños: $puntosEmpeño"
                        tvPuntosReparacion.text = "Puntos Reparación: $puntosReparacion"
                        tvTotalPuntos.text = "Total de Puntos: $totalPuntos"
                    }
                }
        }
    }

    private fun cargarHistorial() {
        uid?.let {
            db.collection("usuarios_puntos").document(it)
                .collection("historial")
                .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, _ ->
                    if (snapshots != null) {
                        historialList.clear()
                        for (doc in snapshots) {
                            val transaccion = doc.toObject(TransaccionPuntos::class.java)
                            historialList.add(transaccion)
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
        }
    }
}
