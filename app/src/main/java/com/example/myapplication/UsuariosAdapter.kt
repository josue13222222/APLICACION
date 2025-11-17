package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsuariosAdapter(
    private val context: Context,
    private val usuariosList: MutableList<Usuario>,
    private val onUsuarioSelected: (Usuario) -> Unit = {}
) : RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuariosList[position]
        holder.tvNombre.text = usuario.nombre
        holder.tvEmail.text = usuario.email
        holder.tvEstado.text = if (usuario.activo) "Activo" else "Bloqueado"
        holder.tvPuntos.text = "${usuario.puntos} pts"

        holder.itemView.setOnClickListener {
            selectedPosition = position
            onUsuarioSelected(usuario)
            notifyDataSetChanged()
        }

        holder.itemView.setBackgroundColor(
            if (selectedPosition == position)
                context.getColor(android.R.color.holo_blue_light)
            else
                context.getColor(android.R.color.white)
        )
    }

    override fun getItemCount(): Int = usuariosList.size

    fun getSelectedUsuario(): Usuario? {
        return if (selectedPosition >= 0 && selectedPosition < usuariosList.size) {
            usuariosList[selectedPosition]
        } else {
            null
        }
    }

    fun updateList(newList: MutableList<Usuario>) {
        usuariosList.clear()
        usuariosList.addAll(newList)
        notifyDataSetChanged()
    }

    class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
        val tvPuntos: TextView = itemView.findViewById(R.id.tvPuntos)
    }
}
