package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class NotificacionesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        // Configuramos RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Creamos una lista de notificaciones simuladas
        val notificaciones = mutableListOf(
            Notificacion("Pedido entregado", "Tu pedido #123 ya llegó.", "Hoy, 9:05 AM"),
            Notificacion("Nuevo cupón disponible", "¡Tienes un nuevo cupón para ti!", "Hoy, 10:00 AM"),
            Notificacion("Actualización importante", "Tu cuenta ha sido actualizada.", "Ayer, 4:30 PM")
        )

        // Asociamos la lista con el RecyclerView usando un Adapter
        val adapter = NotificacionesAdapter(notificaciones)
        recyclerView.adapter = adapter
    }
}
