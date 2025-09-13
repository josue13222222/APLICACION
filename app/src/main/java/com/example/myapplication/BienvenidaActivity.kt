package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BienvenidaActivity : AppCompatActivity() {

    private lateinit var tvBienvenida: TextView
    private lateinit var tvPuntos: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bienvenida)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Referencias a vistas
        tvBienvenida = findViewById(R.id.textoBienvenida)
        tvPuntos = findViewById(R.id.textoPuntos)

        // Mostrar datos del usuario
        auth.currentUser?.let { currentUser ->
            val uid = currentUser.uid
            firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    val nombre = document.getString("nombre")
                    val puntosCupones = document.getLong("puntosCupones") ?: 0
                    tvBienvenida.text = if (!nombre.isNullOrEmpty()) "¡Bienvenido, $nombre!" else "¡Bienvenido!"
                    tvPuntos.text = "Tienes $puntosCupones puntos de cupones"
                }
                .addOnFailureListener {
                    tvBienvenida.text = "¡Bienvenido!"
                    tvPuntos.text = "Tienes 0 puntos de cupones"
                }
        } ?: run {
            tvBienvenida.text = "¡Bienvenido!"
            tvPuntos.text = "Tienes 0 puntos actualmente"
        }

        // Animación latido infinito del logo
        val logo = findViewById<ImageView>(R.id.logoEmpresa)
        val latidoAnim = AnimationUtils.loadAnimation(this, R.anim.latido_logo).apply {
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
        }
        logo.startAnimation(latidoAnim)

        // Botones
        val btnMicuenta = findViewById<Button>(R.id.btnMicuenta)
        val btnServicioTecnico = findViewById<Button>(R.id.btnRobotRescate)
        val btnMas = findViewById<Button>(R.id.btnMas)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)
        val btnEmpenosActivity = findViewById<Button>(R.id.btnEmpenos)
        val btnCupones = findViewById<Button>(R.id.btnCupones) // ← ESTE ES EL BOTÓN “MI CUPÓN”

        // Funcionalidad de botones
        btnMicuenta.setOnClickListener {
            startActivity(Intent(this, MiCuentaActivity::class.java))
        }

        btnServicioTecnico.setOnClickListener {
            startActivity(Intent(this, ServicioTecnicoActivity::class.java))
        }

        btnMas.setOnClickListener {
            startActivity(Intent(this, MasActivity::class.java))
        }

        btnEmpenosActivity.setOnClickListener {
            startActivity(Intent(this, EmpenosActivity::class.java))
        }

        // Botón Mis Cupones
        btnCupones.setOnClickListener {
            startActivity(Intent(this, MisPuntosActivity::class.java))
        }

        // Logo de Facebook con enlace
        val logoFacebook = findViewById<ImageView>(R.id.logoFacebook)
        logoFacebook.setOnClickListener {
            val url = "https://www.facebook.com/profile.php?id=100063654114002"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        // Cerrar sesión
        btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
