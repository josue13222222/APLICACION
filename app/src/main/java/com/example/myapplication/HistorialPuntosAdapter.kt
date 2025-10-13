package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistorialPuntosAdapter(
    private val historialList: List<HistorialPunto>
) : RecyclerView.Adapter<HistorialPuntosAdapter.HistorialViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_punto, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val item = historialList[position]
        holder.tvTipo.text = item.tipo
        holder.tvDescripcion.text = item.descripcion
        holder.tvPuntos.text = "+${item.puntos} puntos"
        holder.tvFecha.text = item.fecha.substring(0, minOf(10, item.fecha.length))
    }

    override fun getItemCount(): Int = historialList.size

    class HistorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTipo: TextView = itemView.findViewById(R.id.tvTipo)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        val tvPuntos: TextView = itemView.findViewById(R.id.tvPuntos)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
    }
}
