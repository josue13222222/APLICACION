package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminGestionReparacionesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrdenAdapter
    private lateinit var tvNoOrdenes: TextView
    private val listaOrdenes = mutableListOf<OrdenServicio>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("[v0]", "AdminGestionReparacionesActivity iniciada")
        setContentView(R.layout.activity_admin_gestion_reparaciones)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.recyclerViewReparaciones)
        tvNoOrdenes = findViewById(R.id.tvNoOrdenes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = OrdenAdapter(
            listaOrdenes,
            onActualizarEstado = { orden, nuevoEstado ->
                actualizarEstado(orden, nuevoEstado)
            },
            onEliminarClick = { orden ->
                confirmarEliminar(orden)
            }
        )

        recyclerView.adapter = adapter
        Log.d("[v0]", "RecyclerView configurado, cargando órdenes...")
        cargarOrdenes()
    }

    private fun cargarOrdenes() {
        db.collection("ordenes_reparacion")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("[v0]", "Error al cargar órdenes: ${error.message}")
                    Toast.makeText(this, "Error al cargar órdenes", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                listaOrdenes.clear()
                Log.d("[v0]", "Documentos encontrados: ${snapshot?.documents?.size ?: 0}")

                snapshot?.documents?.forEach { doc ->
                    val orden = doc.toObject(OrdenServicio::class.java)
                    orden?.let {
                        it.id = doc.id
                        listaOrdenes.add(it)
                        Log.d("[v0]", "Orden cargada: ${it.id} - ${it.nombre}")
                    }
                }

                if (listaOrdenes.isEmpty()) {
                    tvNoOrdenes.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    Log.d("[v0]", "No hay órdenes para mostrar")
                } else {
                    tvNoOrdenes.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    Log.d("[v0]", "Mostrando ${listaOrdenes.size} órdenes")
                }

                adapter.notifyDataSetChanged()
            }
    }

    private fun actualizarEstado(orden: OrdenServicio, nuevoEstado: String) {
        Log.d("[v0]", "Actualizando estado de orden ${orden.id} a: $nuevoEstado")
        db.collection("ordenes_reparacion").document(orden.id)
            .update("estado", nuevoEstado)
            .addOnSuccessListener {
                Toast.makeText(this, "Estado actualizado a: $nuevoEstado", Toast.LENGTH_SHORT).show()
                Log.d("[v0]", "Estado actualizado exitosamente")

                if (nuevoEstado == "Listo") {
                    crearNotificacionReparacion(orden)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar estado", Toast.LENGTH_SHORT).show()
                Log.e("[v0]", "Error al actualizar estado: ${e.message}")
            }
    }

    private fun confirmarEliminar(orden: OrdenServicio) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Orden")
            .setMessage("¿Estás seguro de eliminar esta orden?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarOrden(orden)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarOrden(orden: OrdenServicio) {
        Log.d("[v0]", "Eliminando orden: ${orden.id}")
        db.collection("ordenes_reparacion").document(orden.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Orden eliminada correctamente", Toast.LENGTH_SHORT).show()
                Log.d("[v0]", "Orden eliminada exitosamente")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar orden", Toast.LENGTH_SHORT).show()
                Log.e("[v0]", "Error al eliminar orden: ${e.message}")
            }
    }

    private fun crearNotificacionReparacion(orden: OrdenServicio) {
        val notificacion = hashMapOf(
            "titulo" to "Reparación Lista",
            "descripcion" to "Tu reparación de ${orden.equipo} está lista para recoger",
            "fecha" to java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
            "userId" to orden.userId,
            "tipo" to "reparacion",
            "referenceId" to orden.id,
            "leida" to false,
            "fechaCreacion" to com.google.firebase.Timestamp.now()
        )

        db.collection("notificaciones").document()
            .set(notificacion)
            .addOnSuccessListener {
                Log.d("[v0]", "Notificación de reparación creada para usuario: ${orden.userId}")
            }
            .addOnFailureListener { e ->
                Log.e("[v0]", "Error creando notificación: ${e.message}")
            }
    }
}
