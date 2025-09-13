package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EmpenosAdapter(private val items: List<Empeno>) : RecyclerView.Adapter<EmpenosAdapter.VH>() {
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvProducto: TextView = view.findViewById(R.id.tvProducto)
        val tvDetalle: TextView = view.findViewById(R.id.tvDetalle)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_empeno, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = items[position]
        holder.tvProducto.text = e.producto
        holder.tvDetalle.text = "${e.estado} • S/${e.valor} • ${e.puntos} pts"
        holder.tvFecha.text = e.fecha
    }

    override fun getItemCount(): Int = items.size
}
