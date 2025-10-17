package com.example.myapplication

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MisOrdenesAdapter(private val ordenes: List<OrdenServicio>) :
    RecyclerView.Adapter<MisOrdenesAdapter.OrdenViewHolder>() {

    class OrdenViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrdenId: TextView = view.findViewById(R.id.tvOrdenId)
        val tvEquipo: TextView = view.findViewById(R.id.tvEquipo)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val imgFoto1: ImageView = view.findViewById(R.id.imgFoto1)
        val imgFoto2: ImageView = view.findViewById(R.id.imgFoto2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mis_ordenes, parent, false)
        return OrdenViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrdenViewHolder, position: Int) {
        val orden = ordenes[position]

        holder.tvOrdenId.text = "Orden: ${orden.id}"
        holder.tvEquipo.text = "Equipo: ${orden.equipo}"
        holder.tvEstado.text = "Estado: ${orden.estado}"

        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(orden.timestamp))
        holder.tvFecha.text = "Fecha: $fecha"

        // Mostrar imágenes
        if (orden.imagenes.isNotEmpty()) {
            val foto1Base64 = orden.imagenes[0]
            if (foto1Base64.isNotEmpty()) {
                val decodedBytes = Base64.decode(foto1Base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                holder.imgFoto1.setImageBitmap(bitmap)
                holder.imgFoto1.visibility = View.VISIBLE
            }
        }

        if (orden.imagenes.size > 1) {
            val foto2Base64 = orden.imagenes[1]
            if (foto2Base64.isNotEmpty()) {
                val decodedBytes = Base64.decode(foto2Base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                holder.imgFoto2.setImageBitmap(bitmap)
                holder.imgFoto2.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount() = ordenes.size
}
