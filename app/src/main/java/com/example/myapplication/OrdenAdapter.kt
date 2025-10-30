package com.example.myapplication

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
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
        val tvEstadoActual: TextView = itemView.findViewById(R.id.tvEstadoActual)
        val btnEnReparacion: Button = itemView.findViewById(R.id.btnEnReparacion)
        val btnListoRecoger: Button = itemView.findViewById(R.id.btnListoRecoger)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminar)
        val imgFoto1: ImageView = itemView.findViewById(R.id.imgFoto1)
        val imgFoto2: ImageView = itemView.findViewById(R.id.imgFoto2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_orden_servicio, parent, false)
        return OrdenViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrdenViewHolder, position: Int) {
        val orden = listaOrdenes[position]
        holder.tvNombre.text = orden.nombre
        holder.tvEquipo.text = "Equipo: ${orden.equipo}"
        holder.tvProblema.text = "Problema: ${orden.problema}"
        holder.tvTelefono.text = "Tel: ${orden.telefono}"

        holder.tvEstadoActual.text = "Estado: ${orden.estado}"

        holder.btnEnReparacion.setOnClickListener {
            onActualizarEstado(orden, "En reparación")
        }

        holder.btnListoRecoger.setOnClickListener {
            onActualizarEstado(orden, "Listo para recoger")
        }

        holder.btnEliminar.setOnClickListener {
            onEliminarClick(orden)
        }

        if (orden.imagenes.isNotEmpty()) {
            // Primera imagen
            if (orden.imagenes.size > 0 && orden.imagenes[0].isNotEmpty()) {
                try {
                    val decodedBytes = Base64.decode(orden.imagenes[0], Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    holder.imgFoto1.setImageBitmap(bitmap)
                    holder.imgFoto1.visibility = View.VISIBLE
                    holder.imgFoto1.setOnClickListener {
                        val intent = Intent(holder.itemView.context, VerImagenFullscreenActivity::class.java)
                        intent.putExtra("imagen_base64", orden.imagenes[0])
                        holder.itemView.context.startActivity(intent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    holder.imgFoto1.visibility = View.GONE
                }
            } else {
                holder.imgFoto1.visibility = View.GONE
            }

            // Segunda imagen
            if (orden.imagenes.size > 1 && orden.imagenes[1].isNotEmpty()) {
                try {
                    val decodedBytes = Base64.decode(orden.imagenes[1], Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    holder.imgFoto2.setImageBitmap(bitmap)
                    holder.imgFoto2.visibility = View.VISIBLE
                    holder.imgFoto2.setOnClickListener {
                        val intent = Intent(holder.itemView.context, VerImagenFullscreenActivity::class.java)
                        intent.putExtra("imagen_base64", orden.imagenes[1])
                        holder.itemView.context.startActivity(intent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    holder.imgFoto2.visibility = View.GONE
                }
            } else {
                holder.imgFoto2.visibility = View.GONE
            }
        } else {
            holder.imgFoto1.visibility = View.GONE
            holder.imgFoto2.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = listaOrdenes.size
}
