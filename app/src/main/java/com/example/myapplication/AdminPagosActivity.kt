package com.example.myapplication

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminPagosActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminPagosAdapter
    private lateinit var tvTotalPagos: TextView
    private lateinit var tvTotalMonto: TextView
    private lateinit var spinnerFiltro: Spinner

    private val pagosList = mutableListOf<Pago>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_pagos)

        firestore = FirebaseFirestore.getInstance()

        initViews()
        setupRecyclerView()
        setupFiltros()
        cargarPagos()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerPagos)
        tvTotalPagos = findViewById(R.id.tvTotalPagos)
        tvTotalMonto = findViewById(R.id.tvTotalMonto)
        spinnerFiltro = findViewById(R.id.spinnerFiltro)
    }

    private fun setupRecyclerView() {
        adapter = AdminPagosAdapter(pagosList) { pago ->
            // Callback para actualizar estado del pago
            actualizarEstadoPago(pago)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupFiltros() {
        val filtros = arrayOf("Todos", "Pendiente", "Confirmado", "Rechazado")
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, filtros)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFiltro.adapter = adapterSpinner

        spinnerFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val filtroSeleccionado = filtros[position]
                filtrarPagos(filtroSeleccionado)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun cargarPagos() {
        firestore.collection("pagos")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error al cargar pagos", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                pagosList.clear()
                snapshot?.documents?.forEach { document ->
                    val pago = document.toObject(Pago::class.java)
                    pago?.let { pagosList.add(it) }
                }

                adapter.notifyDataSetChanged()
                actualizarEstadisticas()
            }
    }

    private fun filtrarPagos(filtro: String) {
        val pagosFiltrados = if (filtro == "Todos") {
            pagosList
        } else {
            pagosList.filter { it.estado == filtro }
        }

        adapter.updateList(pagosFiltrados)
        actualizarEstadisticas(pagosFiltrados)
    }

    private fun actualizarEstadisticas(lista: List<Pago> = pagosList) {
        tvTotalPagos.text = "Total: ${lista.size} pagos"
        val montoTotal = lista.sumOf { it.monto }
        tvTotalMonto.text = "Monto: S/ ${"%.2f".format(montoTotal)}"
    }

    private fun actualizarEstadoPago(pago: Pago) {
        val opciones = arrayOf("Confirmado", "Rechazado")

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Actualizar Estado")
        builder.setItems(opciones) { _, which ->
            val nuevoEstado = opciones[which]

            firestore.collection("pagos").document(pago.id)
                .update("estado", nuevoEstado)
                .addOnSuccessListener {
                    if (nuevoEstado == "Confirmado") {
                        agregarPuntosAlUsuario(pago)
                    }
                    Toast.makeText(this, "Estado actualizado a $nuevoEstado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
        }
        builder.show()
    }

    private fun agregarPuntosAlUsuario(pago: Pago) {
        val puntosGanados = (pago.monto / 100).toInt()

        if (puntosGanados > 0 && pago.correoUsuario.isNotEmpty()) {
            firestore.collection("usuarios")
                .whereEqualTo("correo", pago.correoUsuario)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.documents.isNotEmpty()) {
                        val usuarioDoc = querySnapshot.documents[0]
                        val usuarioId = usuarioDoc.id

                        // Sumar puntos al usuario encontrado
                        firestore.collection("usuarios").document(usuarioId)
                            .update("puntos", com.google.firebase.firestore.FieldValue.increment(puntosGanados.toLong()))
                            .addOnSuccessListener {
                                Toast.makeText(this, "✅ Puntos sumados: +$puntosGanados", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al sumar puntos", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al buscar usuario", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
