package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminCuponesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var txtTotalUsuarios: TextView
    private lateinit var txtPuntosTotal: TextView

    private var usuariosList = mutableListOf<Usuario>()
    private var usuariosListFull = mutableListOf<Usuario>() // Lista completa para búsqueda
    private lateinit var usuariosAdapter: UsuariosPuntosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_cupones)

        db = FirebaseFirestore.getInstance()
        initViews()
        setupRecyclerView()
        loadUsuariosConPuntos()
        setupSearch()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerUsuariosPuntos)
        searchEditText = findViewById(R.id.etBuscarUsuario)
        txtTotalUsuarios = findViewById(R.id.txtTotalUsuarios)
        txtPuntosTotal = findViewById(R.id.txtPuntosTotal)
    }

    private fun setupRecyclerView() {
        usuariosAdapter = UsuariosPuntosAdapter(this, usuariosList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = usuariosAdapter
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                buscarUsuarioPuntos(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun buscarUsuarioPuntos(query: String) {
        val filtered = usuariosListFull.filter { usuario ->
            usuario.nombre.contains(query, ignoreCase = true) ||
                    usuario.email.contains(query, ignoreCase = true) ||
                    usuario.telefono.contains(query, ignoreCase = true)
        }.toMutableList()

        usuariosList.clear()
        usuariosList.addAll(filtered)
        usuariosAdapter.notifyDataSetChanged()
    }

    private fun loadUsuariosConPuntos() {
        db.collection("usuarios")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                usuariosListFull.clear()
                usuariosList.clear()
                var puntosTotal = 0

                snapshots?.documents?.forEach { doc ->
                    val usuario = Usuario(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "Sin nombre",
                        email = doc.getString("email") ?: "",
                        telefono = doc.getString("telefono") ?: "",
                        puntos = doc.getLong("puntos")?.toInt() ?: 0
                    )
                    usuariosListFull.add(usuario)
                    usuariosList.add(usuario)
                    puntosTotal += usuario.puntos
                }

                txtTotalUsuarios.text = "Total Usuarios: ${usuariosList.size}"
                txtPuntosTotal.text = "Puntos Totales: $puntosTotal"
                usuariosAdapter.notifyDataSetChanged()
            }
    }
}
