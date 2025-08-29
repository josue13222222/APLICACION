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
    private lateinit var btnVerContrasena: ImageButton
    private lateinit var btnBuscarUsuario: ImageButton
    private lateinit var login: Button
    private lateinit var registro: Button
    private lateinit var olvidoContrasena: TextView
    private lateinit var btnFacebook: ImageButton
    private lateinit var logoImage: ImageView

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
        btnVerContrasena = findViewById(R.id.btnVerContrasena)
        btnBuscarUsuario = findViewById(R.id.btnBuscarUsuario)
        login = findViewById(R.id.login)
        registro = findViewById(R.id.registro)
        olvidoContrasena = findViewById(R.id.olvidasteContrasena)
        logoImage = findViewById(R.id.robot)

        iniciarAnimacionLatido()
        cargarCorreoGuardado()

        if (auth.currentUser != null) {
            abrirBienvenida()
        }

        login.setOnClickListener {
            val usuarioTexto = usuario.text.toString().trim()
            val passwordTexto = contrasena.text.toString().trim()
            if (!validarEmail(usuarioTexto) || passwordTexto.length < 6) {
                Toast.makeText(this, "Correo inválido o contraseña muy corta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loginUser(usuarioTexto, passwordTexto)
        }

        registro.setOnClickListener {
            val email = usuario.text.toString().trim()
            val password = contrasena.text.toString().trim()
            if (!validarEmail(email) || password.length < 6) {
                Toast.makeText(this, "Correo inválido o contraseña muy corta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            registrarUsuario(email, password)
        }

        olvidoContrasena.setOnClickListener {
            val email = usuario.text.toString().trim()
            if (email.isNotEmpty()) {
                resetPassword(email)
            } else {
                Toast.makeText(this, "Por favor, ingresa tu correo", Toast.LENGTH_SHORT).show()
            }
        }

        btnVerContrasena.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            actualizarVisibilidadContrasena()
        }

        btnBuscarUsuario.setOnClickListener {
            buscarUsuariosPorTelefono()
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

    private fun registrarUsuario(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val usuarioData = mapOf(
                            "email" to email,
                            "telefono" to "123456789"
                        )
                        database.child(userId).setValue(usuarioData)
                            .addOnSuccessListener {
                                val firestoreData = hashMapOf(
                                    "email" to email,
                                    "telefono" to "123456789",
                                    "puntos" to 0
                                )
                                firestore.collection("usuarios").document(userId).set(firestoreData)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                        guardarCorreo(email)
                                        abrirBienvenida()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Error en Firestore", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error en Realtime DB", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Error al registrar: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
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

    private fun validarEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun actualizarVisibilidadContrasena() {
        if (isPasswordVisible) {
            contrasena.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnVerContrasena.setImageResource(R.drawable.ic_ojito)
        } else {
            contrasena.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnVerContrasena.setImageResource(R.drawable.ojocerrado)
        }
        contrasena.setSelection(contrasena.text.length)
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

    private fun buscarUsuariosPorTelefono() {
        val telefono = "123456789"
        database.orderByChild("telefono").equalTo(telefono).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val emails = mutableListOf<String>()
                for (userSnapshot in snapshot.children) {
                    val email = userSnapshot.child("email").getValue(String::class.java)
                    if (!email.isNullOrEmpty()) {
                        emails.add(email)
                    }
                }

                if (emails.isNotEmpty()) {
                    mostrarDialogoUsuarios(emails)
                } else {
                    Toast.makeText(this@MainActivity, "No hay usuarios registrados con este teléfono", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error al buscar usuarios", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarDialogoUsuarios(emails: List<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona un correo")
        builder.setItems(emails.toTypedArray()) { _, which ->
            usuario.setText(emails[which])
        }
        builder.show()
    }
}
