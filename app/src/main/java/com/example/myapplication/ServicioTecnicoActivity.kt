package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
class ServicioTecnicoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_servicio_tecnico)

        val btnSolicitar = findViewById<Button>(R.id.btnSolicitarServicio)
        val btnSeguimiento = findViewById<Button>(R.id.btnSeguimiento)

        btnSolicitar.setOnClickListener {
            startActivity(Intent(this, SolicitarServicioActivity::class.java))
        }

        btnSeguimiento.setOnClickListener {
            startActivity(Intent(this, SeguimientoActivity::class.java))
        }
    }
}
