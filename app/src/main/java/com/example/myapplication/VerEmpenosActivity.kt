package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VerEmpenosActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val lista = mutableListOf<Empeno>()
    private lateinit var adapter: EmpenosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_empenos)

        val rv = findViewById<RecyclerView>(R.id.rvEmpenos)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = EmpenosAdapter(this, lista)
        rv.adapter = adapter

        val etTelefono = findViewById<EditText>(R.id.etTelefonoUsuario)
        val btnBuscar = findViewById<Button>(R.id.btnBuscarEmpenos)

        btnBuscar.setOnClickListener {
            val telefono = etTelefono.text.toString().trim()

            if (telefono.isEmpty()) {
                Toast.makeText(this, "Ingresa tu número de teléfono", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!telefono.matches(Regex("^(\\+51|0)?[0-9]{9}$"))) {
                Toast.makeText(this, "Ingresa un número de teléfono válido (+51987654321, 987654321, o 0987654321)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            cargarEmpenos(telefono)
        }
    }

    private fun cargarEmpenos(telefono: String) {
        db.collection("empenos")
            .whereEqualTo("userId", telefono)  // Buscar por teléfono del usuario
            .whereEqualTo("estado", "activo")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d("VerEmpenosActivity", "Error loading empenos: ${error.message}")
                    Toast.makeText(this, "Error al cargar empeños", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                lista.clear()
                snapshot?.forEach { doc ->
                    val e = doc.toObject(Empeno::class.java)
                    lista.add(e)
                }
                adapter.notifyDataSetChanged()

                if (lista.isEmpty()) {
                    Toast.makeText(this, "No hay empeños registrados para este número", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
