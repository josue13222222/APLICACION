package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EmpenosActivity : AppCompatActivity() {
    private lateinit var btnRegistrar: Button
    private lateinit var btnVer: Button
    private lateinit var txtTitulo: TextView

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empenos)

        txtTitulo = findViewById(R.id.txtTitulo)
        btnRegistrar = findViewById(R.id.btnRegistrarEmpeno)
        btnVer = findViewById(R.id.btnVerEmpenos)

        txtTitulo.text = "Bienvenido a Robot Empeños"

        if (auth.currentUser == null) {
            auth.signInAnonymously()
        }

        btnRegistrar.setOnClickListener {
            // Abrir pantalla de registrar empeño
            val intent = Intent(this, RegistrarEmpenoActivity::class.java)
            startActivity(intent)
        }

        btnVer.setOnClickListener {
            // Abrir pantalla de ver empeños
            val intent = Intent(this, VerEmpenosActivity::class.java)
            startActivity(intent)
        }
    }
}
