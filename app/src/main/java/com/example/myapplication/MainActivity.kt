package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import android.animation.ObjectAnimator
import android.animation.ValueAnimator

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var usuario: EditText
    private lateinit var contrasena: EditText
    private lateinit var login: Button
    private lateinit var registro: Button
    private lateinit var olvidoContrasena: TextView
    private lateinit var logoImage: ImageView
    private lateinit var btnAccesoAdmin: Button

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.approbot)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Usuarios")
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)

        usuario = findViewById(R.id.usuario)
        contrasena = findViewById(R.id.contrasena)
        login = findViewById(R.id.login)
        registro = findViewById(R.id.registro)
        olvidoContrasena = findViewById(R.id.olvidasteContrasena)
        logoImage = findViewById(R.id.robot)
        btnAccesoAdmin = findViewById(R.id.btnAccesoAdmin)

        iniciarAnimacionLatido()
        cargarCorreoGuardado()

        if (auth.currentUser != null) {
            abrirBienvenida()
        }

        login.setOnClickListener {
            val usuarioTexto = usuario.text.toString().trim()
            val passwordTexto = contrasena.text.toString().trim()

            if (usuarioTexto.isEmpty() || passwordTexto.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (Patterns.EMAIL_ADDRESS.matcher(usuarioTexto).matches()) {
                // Login con correo
                loginUser(usuarioTexto, passwordTexto)
            } else {
                // Login con celular - buscar el correo asociado
                buscarCorreoPorCelular(usuarioTexto, passwordTexto)
            }
        }

        registro.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        olvidoContrasena.setOnClickListener {
            val email = usuario.text.toString().trim()
            if (email.isNotEmpty()) {
                resetPassword(email)
            } else {
                Toast.makeText(this, "Por favor, ingresa tu correo", Toast.LENGTH_SHORT).show()
            }
        }

        btnAccesoAdmin.setOnClickListener {
            val intent = Intent(this, AdminLoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun buscarCorreoPorCelular(celular: String, password: String) {
        firestore.collection("usuarios")
            .whereEqualTo("celular", celular)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val email = documents.documents[0].getString("email")
                    if (email != null) {
                        loginUser(email, password)
                    } else {
                        Toast.makeText(this, "No se encontró el correo asociado", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "No se encontró usuario con ese número de celular", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al buscar usuario", Toast.LENGTH_SHORT).show()
            }
    }

    private fun iniciarAnimacionLatido() {
        val scaleX = ObjectAnimator.ofFloat(logoImage, "scaleX", 1.0f, 1.2f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(logoImage, "scaleY", 1.0f, 1.2f, 1.0f)
        scaleX.duration = 1000
        scaleY.duration = 1000
        scaleX.repeatCount = ValueAnimator.INFINITE
        scaleY.repeatCount = ValueAnimator.INFINITE
        scaleX.repeatMode = ValueAnimator.REVERSE
        scaleY.repeatMode = ValueAnimator.REVERSE
        scaleX.start()
        scaleY.start()
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    guardarCorreo(email)
                    abrirBienvenida()
                } else {
                    Toast.makeText(this, "Error al iniciar sesión: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Correo de recuperación enviado a $email", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun abrirBienvenida() {
        val intent = Intent(this, BienvenidaActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun guardarCorreo(email: String) {
        with(sharedPreferences.edit()) {
            putString("USER_EMAIL", email)
            apply()
        }
    }

    private fun cargarCorreoGuardado() {
        val savedEmail = sharedPreferences.getString("USER_EMAIL", "")
        if (!savedEmail.isNullOrEmpty()) {
            usuario.setText(savedEmail)
        }
    }
}
