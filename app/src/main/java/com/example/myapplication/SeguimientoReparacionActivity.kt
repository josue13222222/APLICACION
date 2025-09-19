package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SeguimientoReparacionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SeguimientoReparacionAdapter
    private val ordenesList = mutableListOf<OrdenReparacion>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seguimiento_reparacion)

        recyclerView = findViewById(R.id.recyclerSeguimiento)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SeguimientoReparacionAdapter(ordenesList)
        recyclerView.adapter = adapter

        cargarOrdenes()
    }

    private fun cargarOrdenes() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("ordenes_reparacion")
            .whereEqualTo("userId", userId)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error al cargar órdenes", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                ordenesList.clear()
                snapshots?.forEach { document ->
                    val orden = document.toObject(OrdenReparacion::class.java)
                    orden.id = document.id
                    ordenesList.add(orden)
                }
                adapter.notifyDataSetChanged()
            }
    }
}
