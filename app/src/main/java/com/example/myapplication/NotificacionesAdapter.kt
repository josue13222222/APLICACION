package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class NotificacionesAdapter(private val notificaciones: List<Notificacion>) : RecyclerView.Adapter<NotificacionesAdapter.NotificacionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notificacion, parent, false)
        return NotificacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        val notificacion = notificaciones[position]
        holder.titulo.text = notificacion.titulo
        holder.descripcion.text = notificacion.descripcion
        holder.fecha.text = notificacion.fecha

        if (!notificacion.leida) {
            holder.itemView.setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_blue_light))
        } else {
            holder.itemView.setBackgroundColor(holder.itemView.context.getColor(android.R.color.white))
        }
    }

    override fun getItemCount(): Int {
        return notificaciones.size
    }

    class NotificacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.titulo)
        val descripcion: TextView = itemView.findViewById(R.id.descripcion)
        val fecha: TextView = itemView.findViewById(R.id.fecha)
    }
}
