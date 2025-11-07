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
        val tvNombre: TextView? = itemView.findViewById(R.id.tvNombre)
        val tvEquipo: TextView? = itemView.findViewById(R.id.tvEquipo)
        val tvProblema: TextView? = itemView.findViewById(R.id.tvProblema)
        val tvTelefono: TextView? = itemView.findViewById(R.id.tvTelefono)
        val tvEstadoActual: TextView? = itemView.findViewById(R.id.tvEstadoActual)
        val btnEnReparacion: Button? = itemView.findViewById(R.id.btnEnReparacion)
        val btnListoRecoger: Button? = itemView.findViewById(R.id.btnListoRecoger)
        val btnEliminar: Button? = itemView.findViewById(R.id.btnEliminar)
        val imgFoto1: ImageView? = itemView.findViewById(R.id.imgFoto1)
        val imgFoto2: ImageView? = itemView.findViewById(R.id.imgFoto2)
        val imgFoto3: ImageView? = itemView.findViewById(R.id.imgFoto3)
        val imgFoto4: ImageView? = itemView.findViewById(R.id.imgFoto4)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_orden_servicio, parent, false)
        return OrdenViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrdenViewHolder, position: Int) {
        val orden = listaOrdenes[position]

        holder.tvNombre?.text = orden.nombre
        holder.tvEquipo?.text = "Equipo: ${orden.equipo}"
        holder.tvProblema?.text = "Problema: ${orden.problema}"
        holder.tvTelefono?.text = "Tel: ${orden.telefono}"
        holder.tvEstadoActual?.text = "Estado: ${orden.estado}"

        holder.btnEnReparacion?.setOnClickListener {
            onActualizarEstado(orden, "En reparación")
        }

        holder.btnListoRecoger?.setOnClickListener {
            onActualizarEstado(orden, "Listo para recoger")
        }

        holder.btnEliminar?.setOnClickListener {
            onEliminarClick(orden)
        }

        val imagenesToShow = listOf(
            Triple(holder.imgFoto1, 0, "Foto 1"),
            Triple(holder.imgFoto2, 1, "Foto 2"),
            Triple(holder.imgFoto3, 2, "Foto 3"),
            Triple(holder.imgFoto4, 3, "Foto 4")
        )

        for ((imageView, index, label) in imagenesToShow) {
            if (imageView == null) continue

            if (orden.imagenes.isNotEmpty() && orden.imagenes.size > index && orden.imagenes[index].isNotEmpty()) {
                try {
                    val decodedBytes = Base64.decode(orden.imagenes[index], Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    imageView.setImageBitmap(bitmap)
                    imageView.visibility = View.VISIBLE

                    imageView.setOnClickListener {
                        val intent = Intent(holder.itemView.context, VerImagenFullscreenActivity::class.java)
                        intent.putExtra("imagen_base64", orden.imagenes[index])
                        intent.putExtra("titulo", label)
                        holder.itemView.context.startActivity(intent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    imageView.visibility = View.GONE
                }
            } else {
                imageView.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = listaOrdenes.size
}
