package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.animation.ObjectAnimator
import android.animation.ValueAnimator

class AdminLoginActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    private val ADMIN_EMAIL = "admin@tienda.com"
    private val ADMIN_PASSWORD = "admin123"

    private lateinit var adminEmail: EditText
    private lateinit var adminPassword: EditText
    private lateinit var btnVerContrasena: ImageButton
    private lateinit var btnLoginAdmin: Button
    private lateinit var btnVolverLogin: Button
    private lateinit var olvidoContrasena: TextView
    private lateinit var logoImage: ImageView

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        sharedPreferences = getSharedPreferences("AdminLoginPrefs", MODE_PRIVATE)

        adminEmail = findViewById(R.id.adminEmail)
        adminPassword = findViewById(R.id.adminPassword)
        btnVerContrasena = findViewById(R.id.btnVerContrasenaAdmin)
        btnLoginAdmin = findViewById(R.id.btnLoginAdmin)
        btnVolverLogin = findViewById(R.id.btnVolverLogin)
        olvidoContrasena = findViewById(R.id.olvidasteContrasenaAdmin)
        logoImage = findViewById(R.id.robotAdmin)

        iniciarAnimacionLatido()
        cargarCorreoGuardado()

        btnLoginAdmin.setOnClickListener {
            val email = adminEmail.text.toString().trim()
            val password = adminPassword.text.toString().trim()

            loginAdmin(email, password)
        }

        btnVolverLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        olvidoContrasena.setOnClickListener {
            Toast.makeText(this, "Contacta al desarrollador para recuperar la contraseña", Toast.LENGTH_LONG).show()
        }

        btnVerContrasena.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            actualizarVisibilidadContrasena()
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

    private fun verificarSesionAdmin() {
        // No verificar sesión automáticamente, siempre pedir credenciales
    }

    private fun loginAdmin(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (email == ADMIN_EMAIL && password == ADMIN_PASSWORD) {
            with(sharedPreferences.edit()) {
                putBoolean("IS_ADMIN_LOGGED_IN", true)
                apply()
            }
            guardarCorreo(email)
            Toast.makeText(this, "Bienvenido Administrador", Toast.LENGTH_SHORT).show()
            abrirPanelAdmin()
        } else {
            Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_LONG).show()
        }
    }

    private fun abrirPanelAdmin() {
        val intent = Intent(this, AdminDashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun validarEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun actualizarVisibilidadContrasena() {
        if (isPasswordVisible) {
            adminPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnVerContrasena.setImageResource(R.drawable.ic_ojito)
        } else {
            adminPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnVerContrasena.setImageResource(R.drawable.ojocerrado)
        }
        adminPassword.setSelection(adminPassword.text.length)
    }

    private fun guardarCorreo(email: String) {
        with(sharedPreferences.edit()) {
            putString("ADMIN_EMAIL", email)
            apply()
        }
    }

    private fun cargarCorreoGuardado() {
        val savedEmail = sharedPreferences.getString("ADMIN_EMAIL", "")
        if (!savedEmail.isNullOrEmpty()) {
            adminEmail.setText(savedEmail)
        } else {
            adminEmail.setText(ADMIN_EMAIL)
        }
    }
}
