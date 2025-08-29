package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminPanelActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var ordenAdapter: OrdenAdapter
    private val listaOrdenes = mutableListOf<OrdenServicio>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        ordenAdapter = OrdenAdapter(
            listaOrdenes,
            onActualizarEstado = { orden, nuevoEstado ->
                val ordenRef = db.collection("ordenes_reparacion").document(orden.id)
                ordenRef.update("estado", nuevoEstado)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Estado actualizado a $nuevoEstado", Toast.LENGTH_SHORT).show()
                        cargarOrdenes()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al actualizar estado: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            },
            onEliminarClick = { orden ->
                val ordenRef = db.collection("ordenes_reparacion").document(orden.id)
                ordenRef.delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Orden eliminada", Toast.LENGTH_SHORT).show()
                        cargarOrdenes()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al eliminar orden: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        )

        recyclerView.adapter = ordenAdapter

        cargarOrdenes()
    }

    private fun cargarOrdenes() {
        db.collection("ordenes_reparacion")
            .get()
            .addOnSuccessListener { result ->
                listaOrdenes.clear()
                for (document in result) {
                    val orden = document.toObject(OrdenServicio::class.java)
                    if (orden != null) {
                        orden.id = document.id  // ¡MUY IMPORTANTE! Asignar el id Firestore
                        listaOrdenes.add(orden)
                        Log.d("AdminPanelActivity", "Orden recuperada: ${orden.nombre}, ${orden.telefono}, ${orden.equipo}, ${orden.problema}, estado: ${orden.estado}")
                    }
                }
                ordenAdapter.notifyDataSetChanged()

                if (listaOrdenes.isEmpty()) {
                    Toast.makeText(this, "No se encontraron órdenes.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar las órdenes: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("AdminPanelActivity", "Error al cargar datos: ${exception.message}")
            }
    }
}
