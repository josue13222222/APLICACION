package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistorialAdapter(private val lista: List<TransaccionPuntos>) :
    RecyclerView.Adapter<HistorialAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTipo: TextView = view.findViewById(R.id.tvTipo)
        val tvPuntos: TextView = view.findViewById(R.id.tvPuntos)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_puntos, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaccion = lista[position]
        holder.tvTipo.text = transaccion.tipo
        holder.tvPuntos.text = "${transaccion.puntos} pts"
        holder.tvFecha.text = transaccion.fecha?.toDate().toString()
        holder.tvDescripcion.text = transaccion.descripcion
    }

    override fun getItemCount() = lista.size
}
