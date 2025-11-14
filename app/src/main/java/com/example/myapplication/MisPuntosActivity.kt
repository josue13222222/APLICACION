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
    private val auth = FirebaseAuth.getInstance()

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

        btnCanjear.setOnClickListener {
            val intent = Intent(this, MisPuntosActivity::class.java)
            startActivity(intent)
        }
    }

    private fun cargarPuntos() {
        val correoUsuario = auth.currentUser?.email ?: return

        db.collection("usuarios")
            .whereEqualTo("correo", correoUsuario)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val usuarioDoc = querySnapshot.documents[0]
                    val puntos = usuarioDoc.getLong("puntos") ?: 0

                    tvPuntosEmpeño.text = "Puntos Acumulados: $puntos"
                    tvPuntosReparacion.text = "Puntos Disponibles: $puntos"
                    tvTotalPuntos.text = "Total de Puntos: $puntos"
                }
            }
    }

    private fun cargarHistorial() {
        val correoUsuario = auth.currentUser?.email ?: return

        db.collection("usuarios")
            .whereEqualTo("correo", correoUsuario)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
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
