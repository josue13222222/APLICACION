package com.example.myapplication

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etCelular: EditText
    private lateinit var etContrasena: EditText
    private lateinit var etConfirmarContrasena: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var btnVolver: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreo)
        etCelular = findViewById(R.id.etCelular)
        etContrasena = findViewById(R.id.etContrasena)
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        btnVolver = findViewById(R.id.btnVolver)

        btnRegistrar.setOnClickListener {
            registrarUsuario()
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun registrarUsuario() {
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val celular = etCelular.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()
        val confirmarContrasena = etConfirmarContrasena.text.toString().trim()

        // Validaciones
        if (nombre.isEmpty()) {
            etNombre.error = "Ingresa tu nombre"
            etNombre.requestFocus()
            return
        }

        if (correo.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.error = "Ingresa un correo válido"
            etCorreo.requestFocus()
            return
        }

        if (celular.isEmpty() || celular.length < 9) {
            etCelular.error = "Ingresa un número de celular válido"
            etCelular.requestFocus()
            return
        }

        if (contrasena.isEmpty() || contrasena.length < 6) {
            etContrasena.error = "La contraseña debe tener al menos 6 caracteres"
            etContrasena.requestFocus()
            return
        }

        if (contrasena != confirmarContrasena) {
            etConfirmarContrasena.error = "Las contraseñas no coinciden"
            etConfirmarContrasena.requestFocus()
            return
        }

        // Registrar en Firebase Auth
        auth.createUserWithEmailAndPassword(correo, contrasena)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Guardar datos en Firestore
                        val usuarioData = hashMapOf(
                            "nombre" to nombre,
                            "email" to correo,
                            "celular" to celular,
                            "puntos" to 0
                        )

                        firestore.collection("usuarios").document(userId).set(usuarioData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Error al registrar: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
