package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DireccionesActivity : AppCompatActivity() {

    private val tiendaLatitud = -17.784419
    private val tiendaLongitud = -63.182667

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direcciones)

        val btnAbrirMapa = findViewById<Button>(R.id.btnAbrirMapa)
        btnAbrirMapa.setOnClickListener {
            abrirEnGoogleMaps()
        }

        val btnLlamar = findViewById<Button>(R.id.btnLlamarTienda)
        btnLlamar.setOnClickListener {
            llamarTienda()
        }

        configurarGaleriaImagenes()
    }

    private fun abrirEnGoogleMaps() {
        try {
            val uri = Uri.parse("https://goo.gl/maps/1sqgEjALaShttuu89?g_st=aw")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        } catch (e: Exception) {
            val uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$tiendaLatitud,$tiendaLongitud")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    private fun llamarTienda() {
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:+51975167294")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se puede realizar la llamada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarGaleriaImagenes() {
        val recyclerView = findViewById<RecyclerView>(R.id.rvImagenesTienda)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val imagenes = listOf(
            R.drawable.fondo,
            R.drawable.monitor
        )

        val adapter = ImagenesGaleriaAdapter(imagenes)
        recyclerView.adapter = adapter
    }
}
