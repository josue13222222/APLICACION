package com.example.myapplication  // Asegúrate de que sea el paquete correcto



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R


class CompraAdapter(private val lista: List<Compra>) :
    RecyclerView.Adapter<CompraAdapter.CompraViewHolder>() {

    inner class CompraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtFecha: TextView = itemView.findViewById(R.id.txtFecha)
        val txtNumeroOrden: TextView = itemView.findViewById(R.id.txtNumeroOrden)
        val txtTotal: TextView = itemView.findViewById(R.id.txtTotal)
        val txtEstado: TextView = itemView.findViewById(R.id.txtEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompraViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compra, parent, false)
        return CompraViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompraViewHolder, position: Int) {
        val compra = lista[position]
        holder.txtFecha.text = "Fecha: ${compra.fecha}"
        holder.txtNumeroOrden.text = "Orden: ${compra.numeroOrden}"
        holder.txtTotal.text = "Total: ${compra.total}"
        holder.txtEstado.text = "Estado: ${compra.estado}"
    }

    override fun getItemCount(): Int = lista.size
}
