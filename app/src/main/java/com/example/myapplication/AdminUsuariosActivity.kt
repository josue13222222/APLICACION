package com.example.myapplication

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminUsuariosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var btnBuscar: Button
    private lateinit var btnBloquear: Button
    private lateinit var btnDesbloquear: Button
    private lateinit var txtTotalUsuarios: TextView
    private lateinit var txtUsuariosActivos: TextView
    private lateinit var txtUsuariosBloqueados: TextView

    private var usuariosList = mutableListOf<Usuario>()
    private lateinit var usuariosAdapter: UsuariosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_usuarios)

        db = FirebaseFirestore.getInstance()
        initViews()
        setupRecyclerView()
        loadUsuarios()
        loadEstadisticas()

        btnBuscar.setOnClickListener { buscarUsuario() }
        btnBloquear.setOnClickListener { cambiarEstadoUsuario(false) }
        btnDesbloquear.setOnClickListener { cambiarEstadoUsuario(true) }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerUsuarios)
        searchEditText = findViewById(R.id.etBuscarUsuario)
        btnBuscar = findViewById(R.id.btnBuscarUsuario)
        btnBloquear = findViewById(R.id.btnBloquearUsuario)
        btnDesbloquear = findViewById(R.id.btnDesbloquearUsuario)
        txtTotalUsuarios = findViewById(R.id.txtTotalUsuarios)
        txtUsuariosActivos = findViewById(R.id.txtUsuariosActivos)
        txtUsuariosBloqueados = findViewById(R.id.txtUsuariosBloqueados)
    }

    private fun setupRecyclerView() {
        usuariosAdapter = UsuariosAdapter(this, usuariosList) { usuario ->
            // Callback para seleccionar usuario
            actualizarBotonesEstado(usuario)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = usuariosAdapter
    }

    private fun loadUsuarios() {
        db.collection("usuarios")
            .orderBy("fechaRegistro", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                usuariosList.clear()
                for (document in documents) {
                    val usuario = document.toObject(Usuario::class.java)
                    usuario.id = document.id
                    usuariosList.add(usuario)
                }
                usuariosAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar usuarios: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadEstadisticas() {
        db.collection("usuarios").get()
            .addOnSuccessListener { documents ->
                val total = documents.size()
                var activos = 0
                var bloqueados = 0

                for (document in documents) {
                    val activo = document.getBoolean("activo") ?: true
                    if (activo) activos++ else bloqueados++
                }

                txtTotalUsuarios.text = "Total: $total"
                txtUsuariosActivos.text = "Activos: $activos"
                txtUsuariosBloqueados.text = "Bloqueados: $bloqueados"
            }
    }

    private fun buscarUsuario() {
        val query = searchEditText.text.toString().trim()
        if (query.isEmpty()) {
            loadUsuarios()
            return
        }

        db.collection("usuarios")
            .whereGreaterThanOrEqualTo("email", query)
            .whereLessThanOrEqualTo("email", query + '\uf8ff')
            .get()
            .addOnSuccessListener { documents ->
                usuariosList.clear()
                for (document in documents) {
                    val usuario = document.toObject(Usuario::class.java)
                    usuario.id = document.id
                    usuariosList.add(usuario)
                }
                usuariosAdapter.notifyDataSetChanged()
            }
    }

    private fun cambiarEstadoUsuario(activo: Boolean) {
        val usuarioSeleccionado = usuariosAdapter.getSelectedUsuario()
        if (usuarioSeleccionado == null) {
            Toast.makeText(this, "Selecciona un usuario primero", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("usuarios").document(usuarioSeleccionado.id)
            .update("activo", activo)
            .addOnSuccessListener {
                val estado = if (activo) "desbloqueado" else "bloqueado"
                Toast.makeText(this, "Usuario $estado exitosamente", Toast.LENGTH_SHORT).show()
                loadUsuarios()
                loadEstadisticas()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarBotonesEstado(usuario: Usuario) {
        btnBloquear.isEnabled = usuario.activo
        btnDesbloquear.isEnabled = !usuario.activo
    }
}
