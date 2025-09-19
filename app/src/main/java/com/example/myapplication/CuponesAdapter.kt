package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CuponesAdapter(
    private val context: Context,
    private val cuponesList: List<Cupon>,
    private val onCuponSelected: (Cupon) -> Unit = {}
) : RecyclerView.Adapter<CuponesAdapter.CuponViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CuponViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_cupon, parent, false)
        return CuponViewHolder(view)
    }

    override fun onBindViewHolder(holder: CuponViewHolder, position: Int) {
        val cupon = cuponesList[position]
        holder.tvCodigo.text = cupon.codigo
        holder.tvDescripcion.text = cupon.descripcion
        holder.tvDescuento.text = "${cupon.descuentoPorcentaje}% OFF"
        holder.tvEstado.text = if (cupon.activo) "Activo" else "Inactivo"
        holder.tvUsos.text = "Usos: ${cupon.usos}"

        holder.itemView.setOnClickListener {
            selectedPosition = position
            onCuponSelected(cupon)
            notifyDataSetChanged()
        }

        holder.itemView.setBackgroundColor(
            if (selectedPosition == position)
                context.getColor(android.R.color.holo_blue_light)
            else
                context.getColor(android.R.color.white)
        )
    }

    override fun getItemCount(): Int = cuponesList.size

    fun getSelectedCupon(): Cupon? {
        return if (selectedPosition >= 0 && selectedPosition < cuponesList.size) {
            cuponesList[selectedPosition]
        } else {
            null
        }
    }

    class CuponViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCodigo: TextView = itemView.findViewById(R.id.tvCodigo)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        val tvDescuento: TextView = itemView.findViewById(R.id.tvDescuento)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
        val tvUsos: TextView = itemView.findViewById(R.id.tvUsos)
    }
}
