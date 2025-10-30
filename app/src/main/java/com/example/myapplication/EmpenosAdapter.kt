package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.BitmapFactory
import android.util.Base64

class EmpenosAdapter(
    private val context: Context,
    private val items: MutableList<Empeno>,
    private val onEmpenoSelected: (Empeno) -> Unit = {}
) : RecyclerView.Adapter<EmpenosAdapter.VH>() {

    private var selectedPosition = -1

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvProducto: TextView = view.findViewById(R.id.tvProducto)
        val tvMonto: TextView = view.findViewById(R.id.tvMonto)
        val tvPrecioMensual: TextView = view.findViewById(R.id.tvPrecioMensual)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val imgProducto: ImageView = view.findViewById(R.id.imgProducto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_empeno, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = items[position]
        holder.tvProducto.text = e.producto
        holder.tvMonto.text = "Empeñado por: S/ ${e.montoEmpenado}"
        holder.tvPrecioMensual.text = "Cobro mensual: S/ ${e.precioMensual}"
        holder.tvFecha.text = "Desde: ${e.fecha}"

        if (e.foto1Url.isNotEmpty()) {
            try {
                val decodedString = Base64.decode(e.foto1Url, Base64.NO_WRAP)
                val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                holder.imgProducto.setImageBitmap(bitmap)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        holder.itemView.setOnClickListener {
            selectedPosition = position
            onEmpenoSelected(e)
            notifyDataSetChanged()
        }

        holder.itemView.setBackgroundColor(
            if (selectedPosition == position)
                context.getColor(android.R.color.holo_blue_light)
            else
                context.getColor(android.R.color.white)
        )
    }

    override fun getItemCount(): Int = items.size

    fun getSelectedEmpeno(): Empeno? {
        return if (selectedPosition >= 0 && selectedPosition < items.size) {
            items[selectedPosition]
        } else {
            null
        }
    }
}
