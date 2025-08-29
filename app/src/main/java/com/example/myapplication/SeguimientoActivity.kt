package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class SeguimientoActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seguimiento)

        val btnBuscar = findViewById<Button>(R.id.btnBuscar)
        val etNumeroOrden = findViewById<EditText>(R.id.etNumeroOrden)
        val tvEstado = findViewById<TextView>(R.id.tvEstado)

        btnBuscar.setOnClickListener {
            val numeroOrden = etNumeroOrden.text.toString().trim()

            if (numeroOrden.isNotEmpty()) {
                tvEstado.visibility = View.GONE // Ocultar antes de buscar
                buscarEstado(numeroOrden, tvEstado)
            } else {
                Toast.makeText(this, "Por favor ingrese un número de orden.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buscarEstado(numeroOrden: String, tvEstado: TextView) {
        db.collection("ordenes_reparacion")
            .document(numeroOrden)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val estado = document.getString("estado") ?: "Estado no disponible"
                    val nombre = document.getString("nombre") ?: "Cliente no identificado"
                    val equipo = document.getString("equipo") ?: "Equipo no especificado"

                    val mensaje = """
                        🔍 Resultado:
                        👤 Cliente: $nombre
                        💻 Equipo: $equipo
                        📌 Estado: $estado
                    """.trimIndent()

                    tvEstado.text = mensaje
                    tvEstado.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, "Orden no encontrada. Verifica el número.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al consultar: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
