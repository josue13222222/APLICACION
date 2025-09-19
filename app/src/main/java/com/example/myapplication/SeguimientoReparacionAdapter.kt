package com.example.myapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SeguimientoReparacionAdapter(
    private val ordenes: List<OrdenReparacion>
) : RecyclerView.Adapter<SeguimientoReparacionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDispositivo: TextView = view.findViewById(R.id.tvDispositivo)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvProblema: TextView = view.findViewById(R.id.tvProblema)
        val tvCosto: TextView = view.findViewById(R.id.tvCosto)
        val tvObservaciones: TextView = view.findViewById(R.id.tvObservaciones)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seguimiento_reparacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orden = ordenes[position]

        holder.tvDispositivo.text = orden.dispositivo
        holder.tvEstado.text = orden.estado
        holder.tvProblema.text = orden.problema
        holder.tvCosto.text = if (orden.costoEstimado > 0) "S/. ${orden.costoEstimado}" else "Por definir"
        holder.tvObservaciones.text = orden.observaciones.ifEmpty { "Sin observaciones" }

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
        orden.fechaActualizacion?.let { timestamp ->
            val fecha = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                .format(timestamp.toDate())
            holder.tvFecha.text = "Actualizado: $fecha"
        }
    }

    override fun getItemCount() = ordenes.size
}
