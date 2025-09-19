package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminOrdenesReparacionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminOrdenesReparacionAdapter
    private val ordenesList = mutableListOf<OrdenReparacion>()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_ordenes_reparacion)

        recyclerView = findViewById(R.id.recyclerAdminOrdenes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdminOrdenesReparacionAdapter(ordenesList) { orden ->
            mostrarDialogoActualizarEstado(orden)
        }
        recyclerView.adapter = adapter

        cargarOrdenes()
    }

    private fun cargarOrdenes() {
        db.collection("ordenes_reparacion")
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

    private fun mostrarDialogoActualizarEstado(orden: OrdenReparacion) {
        val estados = arrayOf("Recibido", "En Diagnóstico", "En Reparación", "Listo", "Entregado")
        val estadoActual = estados.indexOf(orden.estado)

        AlertDialog.Builder(this)
            .setTitle("Actualizar Estado - ${orden.dispositivo}")
            .setSingleChoiceItems(estados, estadoActual) { dialog, which ->
                val nuevoEstado = estados[which]
                actualizarEstadoOrden(orden.id, nuevoEstado)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarEstadoOrden(ordenId: String, nuevoEstado: String) {
        val updates = mapOf(
            "estado" to nuevoEstado,
            "fechaActualizacion" to com.google.firebase.Timestamp.now()
        )

        db.collection("ordenes_reparacion").document(ordenId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Estado actualizado correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar estado", Toast.LENGTH_SHORT).show()
            }
    }
}
