package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class AdminPagosAdapter(
    private var pagosList: List<Pago>,
    private val onPagoClick: (Pago) -> Unit
) : RecyclerView.Adapter<AdminPagosAdapter.PagoViewHolder>() {

    class PagoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardPago: CardView = itemView.findViewById(R.id.cardPago)
        val tvServicio: TextView = itemView.findViewById(R.id.tvServicio)
        val tvMonto: TextView = itemView.findViewById(R.id.tvMonto)
        val tvMetodo: TextView = itemView.findViewById(R.id.tvMetodo)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvReferencia: TextView = itemView.findViewById(R.id.tvReferencia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pago_admin, parent, false)
        return PagoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PagoViewHolder, position: Int) {
        val pago = pagosList[position]

        holder.tvServicio.text = pago.servicioTipo
        holder.tvMonto.text = "S/ ${"%.2f".format(pago.monto)}"
        holder.tvMetodo.text = pago.metodoPago
        holder.tvEstado.text = pago.estado
        holder.tvReferencia.text = "Ref: ${pago.numeroReferencia}"

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.tvFecha.text = dateFormat.format(pago.fecha.toDate())

        // Color según estado
        val context = holder.itemView.context
        when (pago.estado) {
            "Pendiente" -> holder.tvEstado.setTextColor(context.getColor(R.color.orange))
            "Confirmado" -> holder.tvEstado.setTextColor(context.getColor(R.color.green))
            "Rechazado" -> holder.tvEstado.setTextColor(context.getColor(R.color.red))
        }

        holder.cardPago.setOnClickListener {
            onPagoClick(pago)
        }
    }

    override fun getItemCount(): Int = pagosList.size

    fun updateList(newList: List<Pago>) {
        pagosList = newList
        notifyDataSetChanged()
    }
}
