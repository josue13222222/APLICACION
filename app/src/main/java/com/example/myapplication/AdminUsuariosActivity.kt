package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

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
    private var usuariosListCompleta = mutableListOf<Usuario>()
    private lateinit var usuariosAdapter: UsuariosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_usuarios)

        db = FirebaseFirestore.getInstance()
        initViews()
        setupRecyclerView()
        setupSearch()
        loadUsuariosRealTime()  // Cambiar a tiempo real con snapshot listener
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
            actualizarBotonesEstado(usuario)
            verHistorialUsuario(usuario)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = usuariosAdapter
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    usuariosAdapter.updateList(usuariosListCompleta)
                } else {
                    buscarUsuarioDinamico(s.toString())
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadUsuariosRealTime() {
        db.collection("usuarios")
            .orderBy("fechaRegistro", Query.Direction.DESCENDING)
            .addSnapshotListener { documents, exception ->
                if (exception != null) {
                    Toast.makeText(this, "Error al cargar usuarios: ${exception.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                usuariosList.clear()
                usuariosListCompleta.clear()

                if (documents != null) {
                    for (document in documents) {
                        val usuario = document.toObject(Usuario::class.java)
                        usuario.id = document.id
                        usuariosList.add(usuario)
                        usuariosListCompleta.add(usuario)
                    }
                }

                usuariosAdapter.notifyDataSetChanged()
                loadEstadisticas()
            }
    }

    private fun loadEstadisticas() {
        db.collection("usuarios").addSnapshotListener { documents, exception ->
            if (exception != null) {
                return@addSnapshotListener
            }

            if (documents != null) {
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
    }

    private fun buscarUsuario() {
        val query = searchEditText.text.toString().trim()
        if (query.isEmpty()) {
            usuariosAdapter.updateList(usuariosListCompleta)
            return
        }
        buscarUsuarioDinamico(query)
    }

    private fun buscarUsuarioDinamico(query: String) {
        val filtered = usuariosListCompleta.filter { usuario ->
            usuario.nombre.contains(query, ignoreCase = true) ||
                    usuario.email.contains(query, ignoreCase = true) ||
                    usuario.telefono.contains(query, ignoreCase = true)
        }.toMutableList()
        usuariosAdapter.updateList(filtered)
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
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarBotonesEstado(usuario: Usuario) {
        btnBloquear.isEnabled = usuario.activo
        btnDesbloquear.isEnabled = !usuario.activo
    }

    private fun verHistorialUsuario(usuario: Usuario) {
        db.collection("usuarios").document(usuario.id).collection("historial")
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { documents ->
                val historialText = StringBuilder("Historial de ${usuario.nombre}:\n\n")
                if (documents.isEmpty) {
                    historialText.append("Sin historial de transacciones")
                } else {
                    for ((index, doc) in documents.withIndex()) {
                        val tipo = doc.getString("tipo") ?: "Desconocido"
                        val puntos = doc.getLong("puntos") ?: 0
                        val fecha = doc.getLong("fecha") ?: 0
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        val fechaFormato = sdf.format(Date(fecha))
                        historialText.append("${index + 1}. $tipo: $puntos puntos - $fechaFormato\n")
                    }
                }

                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Historial de Transacciones")
                    .setMessage(historialText.toString())
                    .setPositiveButton("Cerrar") { _, _ -> }
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar historial", Toast.LENGTH_SHORT).show()
            }
    }
}
