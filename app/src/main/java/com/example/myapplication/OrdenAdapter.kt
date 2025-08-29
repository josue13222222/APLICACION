package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrdenAdapter(
    private val listaOrdenes: List<OrdenServicio>,
    private val onActualizarEstado: (orden: OrdenServicio, nuevoEstado: String) -> Unit,
    private val onEliminarClick: (orden: OrdenServicio) -> Unit
) : RecyclerView.Adapter<OrdenAdapter.OrdenViewHolder>() {

    class OrdenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvEquipo: TextView = itemView.findViewById(R.id.tvEquipo)
        val tvProblema: TextView = itemView.findViewById(R.id.tvProblema)
        val tvTelefono: TextView = itemView.findViewById(R.id.tvTelefono)
        val btnPendiente: Button = itemView.findViewById(R.id.btnPendiente)
        val btnEnProceso: Button = itemView.findViewById(R.id.btnEnProceso)
        val btnListo: Button = itemView.findViewById(R.id.btnListo)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_orden_servicio, parent, false)
        return OrdenViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrdenViewHolder, position: Int) {
        val orden = listaOrdenes[position]
        holder.tvNombre.text = "Nombre: ${orden.nombre}"
        holder.tvEquipo.text = "Equipo: ${orden.equipo}"
        holder.tvProblema.text = "Problema: ${orden.problema}"
        holder.tvTelefono.text = "Tel: ${orden.telefono}"

        holder.btnPendiente.setOnClickListener {
            onActualizarEstado(orden, "Pendiente")
        }

        holder.btnEnProceso.setOnClickListener {
            onActualizarEstado(orden, "En proceso")
        }

        holder.btnListo.setOnClickListener {
            onActualizarEstado(orden, "Listo para recoger")
        }

        holder.btnEliminar.setOnClickListener {
            onEliminarClick(orden)
        }
    }

    override fun getItemCount(): Int = listaOrdenes.size
}

