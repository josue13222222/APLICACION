package com.example.myapplication
import android.content.Intent
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

class MisPuntosActivity : AppCompatActivity() {

    private lateinit var tvPuntosEmpeño: TextView
    private lateinit var tvPuntosReparacion: TextView
    private lateinit var tvTotalPuntos: TextView
    private lateinit var btnCanjear: Button
    private lateinit var recyclerHistorial: RecyclerView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var sharedPreferences: SharedPreferences

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

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        recyclerHistorial.layoutManager = LinearLayoutManager(this)
        adapter = HistorialAdapter(historialList)
        recyclerHistorial.adapter = adapter

        cargarPuntos()
        cargarHistorial()

        btnCanjear.setOnClickListener {
            val intent = Intent(this, MisPuntosActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarPuntos()
    }

    private fun cargarPuntos() {
        val telefonoUsuario = sharedPreferences.getString("telefono", "") ?: ""

        Log.d("[v0]", "Loading points for phone: $telefonoUsuario")

        if (telefonoUsuario.isEmpty()) {
            tvTotalPuntos.text = "Total de Puntos: 0"
            return
        }

        SistemaPuntos.cargarPuntosDelUsuario(this, telefonoUsuario) { puntos ->
            Log.d("[v0]", "Points loaded: $puntos for $telefonoUsuario")
            tvPuntosEmpeño.text = "Puntos Acumulados: $puntos"
            tvPuntosReparacion.text = "Puntos Disponibles: $puntos"
            tvTotalPuntos.text = "Total de Puntos: $puntos"
        }
    }

    private fun cargarHistorial() {
        val telefonoUsuario = sharedPreferences.getString("telefono", "") ?: ""

        if (telefonoUsuario.isEmpty()) {
            return
        }

        db.collection("usuarios")
            .whereEqualTo("telefono", telefonoUsuario)
            .addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    val usuarioDoc = querySnapshot.documents[0]
                    val uid = usuarioDoc.id

                    db.collection("usuarios").document(uid)
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
}
