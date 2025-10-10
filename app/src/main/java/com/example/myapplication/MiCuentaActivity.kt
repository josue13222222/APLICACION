package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MiCuentaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mi_cuenta)

        val btnDatosPersonales = findViewById<Button>(R.id.btnDatosPersonales)
        val btnNotificaciones = findViewById<Button>(R.id.btnNotificaciones)
        val btnMisCompras = findViewById<Button>(R.id.btnMisCompras)
        val btnDirecciones = findViewById<Button>(R.id.btnDirecciones)
        // Botón Datos Personales
        btnDatosPersonales.setOnClickListener {
            startActivity(Intent(this, DatosPersonalesActivity::class.java))
        }

        // Botón Notificaciones
        btnNotificaciones.setOnClickListener {
            startActivity(Intent(this, NotificacionesActivity::class.java))
        }

        // Botón Mis Compras
        btnMisCompras.setOnClickListener {
            startActivity(Intent(this, MisComprasActivity::class.java))
        }

        // Botón Direcciones
        btnDirecciones.setOnClickListener {
            startActivity(Intent(this, AddressFormActivity::class.java))
        }

    }
}
