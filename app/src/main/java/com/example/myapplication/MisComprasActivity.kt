package com.example.myapplication

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MisComprasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MisOrdenesAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val listaOrdenes = mutableListOf<OrdenServicio>()
    private lateinit var tvTitulo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_compras)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        tvTitulo = findViewById(R.id.txtTitulo)
        recyclerView = findViewById(R.id.recyclerMisCompras)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = MisOrdenesAdapter(listaOrdenes)
        recyclerView.adapter = adapter

        cargarOrdenesDelUsuario()
    }

    private fun cargarOrdenesDelUsuario() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("ordenes_reparacion")
            .whereEqualTo("userId", user.uid)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    println("[v0] Error al cargar órdenes: ${error.message}")
                    return@addSnapshotListener
                }

                listaOrdenes.clear()
                for (doc in documents!!) {
                    try {
                        val orden = doc.toObject(OrdenServicio::class.java)
                        val ordenActualizada = orden.copy(
                            id = if (orden.id.isEmpty()) doc.id else orden.id,
                            ordenId = if (orden.ordenId.isEmpty()) doc.id else orden.ordenId
                        )
                        listaOrdenes.add(ordenActualizada)
                    } catch (e: Exception) {
                        println("[v0] Error al deserializar orden: ${e.message}")
                    }
                }
                adapter.notifyDataSetChanged()

                if (listaOrdenes.isEmpty()) {
                    Toast.makeText(this, "No tienes órdenes de reparación registradas", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
