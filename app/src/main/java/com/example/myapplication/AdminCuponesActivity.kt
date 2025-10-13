package com.example.myapplication

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminCuponesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var txtTotalUsuarios: TextView
    private lateinit var txtPuntosTotal: TextView

    private var usuariosList = mutableListOf<Usuario>()
    private lateinit var usuariosAdapter: UsuariosPuntosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_cupones)

        db = FirebaseFirestore.getInstance()
        initViews()
        setupRecyclerView()
        loadUsuariosConPuntos()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerUsuariosPuntos)
        txtTotalUsuarios = findViewById(R.id.txtTotalUsuarios)
        txtPuntosTotal = findViewById(R.id.txtPuntosTotal)
    }

    private fun setupRecyclerView() {
        usuariosAdapter = UsuariosPuntosAdapter(this, usuariosList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = usuariosAdapter
    }

    private fun loadUsuariosConPuntos() {
        db.collection("users")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                usuariosList.clear()
                var puntosTotal = 0

                snapshots?.documents?.forEach { doc ->
                    val usuario = Usuario(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "Sin nombre",
                        email = doc.getString("email") ?: "",
                        puntos = doc.getLong("points")?.toInt() ?: 0
                    )
                    usuariosList.add(usuario)
                    puntosTotal += usuario.puntos
                }

                txtTotalUsuarios.text = "Total Usuarios: ${usuariosList.size}"
                txtPuntosTotal.text = "Puntos Totales: $puntosTotal"
                usuariosAdapter.notifyDataSetChanged()
            }
    }
}
