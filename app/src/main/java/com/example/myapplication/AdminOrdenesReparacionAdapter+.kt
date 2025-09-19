package com.example.myapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminOrdenesReparacionAdapter(
    private val ordenes: List<OrdenReparacion>,
    private val onActualizarClick: (OrdenReparacion) -> Unit
) : RecyclerView.Adapter<AdminOrdenesReparacionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUsuario: TextView = view.findViewById(R.id.tvUsuario)
        val tvDispositivo: TextView = view.findViewById(R.id.tvDispositivo)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvProblema: TextView = view.findViewById(R.id.tvProblema)
        val btnActualizar: Button = view.findViewById(R.id.btnActualizar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_orden_reparacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orden = ordenes[position]

        holder.tvUsuario.text = "${orden.nombreUsuario} - ${orden.telefono}"
        holder.tvDispositivo.text = orden.dispositivo
        holder.tvEstado.text = orden.estado
        holder.tvProblema.text = orden.problema

        // Color según estado
        val colorEstado = when (orden.estado) {
            "Recibido" -> Color.parseColor("#FF9800")
            "En Diagnóstico" -> Color.parseColor("#2196F3")
            "En Reparación" -> Color.parseColor("#FF5722")
            "Listo" -> Color.parseColor("#4CAF50")
            "Entregado" -> Color.parseColor("#9E9E9E")
            else -> Color.parseColor("#607D8B")
        }
        holder.tvEstado.setTextColor(colorEstado)

        // Formato de fecha
        orden.fechaCreacion?.let { timestamp ->
            val fecha = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                .format(timestamp.toDate())
            holder.tvFecha.text = fecha
        }

        holder.btnActualizar.setOnClickListener {
            onActualizarClick(orden)
        }
    }

    override fun getItemCount() = ordenes.size
}
