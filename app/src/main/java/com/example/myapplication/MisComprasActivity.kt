package com.example.myapplication

import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_compras)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

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
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                listaOrdenes.clear()
                for (doc in documents) {
                    val orden = doc.toObject(OrdenServicio::class.java)
                    orden.id = doc.id
                    listaOrdenes.add(orden)
                }
                adapter.notifyDataSetChanged()

                if (listaOrdenes.isEmpty()) {
                    Toast.makeText(this, "No tienes órdenes registradas", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar órdenes", Toast.LENGTH_SHORT).show()
            }
    }
}
