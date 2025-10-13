package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val mensajes: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layoutId = if (viewType == 1) {
            R.layout.item_mensaje_usuario
        } else {
            R.layout.item_mensaje_robot
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(mensajes[position])
    }

    override fun getItemCount() = mensajes.size

    override fun getItemViewType(position: Int): Int {
        return if (mensajes[position].esUsuario) 1 else 0
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMensaje: TextView = itemView.findViewById(R.id.tvMensaje)

        fun bind(mensaje: ChatMessage) {
            tvMensaje.text = mensaje.texto
        }
    }
}
