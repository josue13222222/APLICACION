package com.example.myapplication

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class RegistrarEmpenoActivity : AppCompatActivity() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_empeno)

        val etProducto = findViewById<EditText>(R.id.etProducto)
        val spinnerEstado = findViewById<Spinner>(R.id.spinnerEstado)
        val etValor = findViewById<EditText>(R.id.etValor)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)

        // llenar spinner
        val estados = arrayOf("Nuevo", "Usado", "Regular")
        spinnerEstado.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)

        btnRegistrar.setOnClickListener {
            val producto = etProducto.text.toString().trim()
            val estado = spinnerEstado.selectedItem.toString()
            val valorStr = etValor.text.toString().trim()

            if (producto.isEmpty() || valorStr.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val valor = valorStr.toIntOrNull()
            if (valor == null || valor <= 0) {
                Toast.makeText(this, "Ingresa un valor válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val puntos = valor / 10 // regla simple: 1 punto por S/10
            val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val uid = user.uid

            // crear objeto
            val docRef = db.collection("empenos").document()
            val empeno = Empeno(
                id = docRef.id,
                producto = producto,
                estado = estado,
                valor = valor,
                puntos = puntos,
                fecha = fecha,
                userId = uid
            )

            // Guardar empeño y actualizar puntos del usuario (merge)
            docRef.set(empeno)
                .addOnSuccessListener {
                    // actualizar puntos del usuario (campo "points")
                    val userDoc = db.collection("users").document(uid)
                    userDoc.set(mapOf("points" to FieldValue.increment(puntos.toLong())), com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Empeño registrado. Ganaste $puntos puntos", Toast.LENGTH_SHORT).show()
                            finish() // regresar
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error actualizando puntos: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error guardando empeño: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
