package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AdminPagosAdapter(
    private var pagosList: List<Pago>,
    private val onPagoClick: (Pago) -> Unit
) : RecyclerView.Adapter<AdminPagosAdapter.PagoViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()

    class PagoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardPago: CardView = itemView.findViewById(R.id.cardPago)
        val tvServicio: TextView = itemView.findViewById(R.id.tvServicio)
        val tvMonto: TextView = itemView.findViewById(R.id.tvMonto)
        val tvMetodo: TextView = itemView.findViewById(R.id.tvMetodo)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvReferencia: TextView = itemView.findViewById(R.id.tvReferencia)
        val tvTelefono: TextView = itemView.findViewById(R.id.tvTelefono)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminar)
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
        holder.tvTelefono.text = "Tel: ${pago.telefonoUsuario}"

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

        if (pago.estado == "Confirmado") {
            holder.btnEliminar.visibility = View.VISIBLE
            holder.btnEliminar.setOnClickListener {
                eliminarPago(pago.id, holder.itemView.context)
            }
        } else {
            holder.btnEliminar.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = pagosList.size

    fun updateList(newList: List<Pago>) {
        pagosList = newList
        notifyDataSetChanged()
    }

    private fun eliminarPago(pagoId: String, context: android.content.Context) {
        firestore.collection("pagos").document(pagoId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "✅ Pago eliminado correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al eliminar: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
