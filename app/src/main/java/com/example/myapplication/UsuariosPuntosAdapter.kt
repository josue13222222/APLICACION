package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsuariosPuntosAdapter(
    private val context: Context,
    private val usuarios: List<Usuario>
) : RecyclerView.Adapter<UsuariosPuntosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNombre: TextView = view.findViewById(R.id.txtUsuarioNombre)
        val txtEmail: TextView = view.findViewById(R.id.txtUsuarioEmail)
        val txtPuntos: TextView = view.findViewById(R.id.txtUsuarioPuntos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_usuario_puntos, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = usuarios[position]

        holder.txtNombre.text = usuario.nombre
        holder.txtEmail.text = usuario.email
        holder.txtPuntos.text = "${usuario.puntos} pts"
    }

    override fun getItemCount() = usuarios.size
}
