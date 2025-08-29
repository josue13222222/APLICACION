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
    private val cuponesList: List<Cupon>
) : RecyclerView.Adapter<CuponesAdapter.CuponViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CuponViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_cupon, parent, false)
        return CuponViewHolder(view)
    }

    override fun onBindViewHolder(holder: CuponViewHolder, position: Int) {
        val cupon = cuponesList[position]
        holder.tvNombre.text = cupon.nombre
        holder.tvTipo.text = cupon.tipo
        holder.tvCodigo.text = cupon.codigo
        holder.tvFechaExpiracion.text = cupon.fechaExpiracion
        holder.tvEstado.text = cupon.estado

        holder.btnCanjear.setOnClickListener {
            // Aquí va la lógica para canjear el cupón
        }
    }

    override fun getItemCount(): Int = cuponesList.size

    class CuponViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvTipo: TextView = itemView.findViewById(R.id.tvTipo)
        val tvCodigo: TextView = itemView.findViewById(R.id.tvCodigo)
        val tvFechaExpiracion: TextView = itemView.findViewById(R.id.tvFechaExpiracion)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
        val btnCanjear: Button = itemView.findViewById(R.id.btnCanjear)
    }
}
