package com.example.myapplication

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EmpenosPendientesAdapter(
    private val context: Context,
    private val items: MutableList<Empeno>,
    private val onAprobar: (Empeno) -> Unit,
    private val onRechazar: (Empeno) -> Unit
) : RecyclerView.Adapter<EmpenosPendientesAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val txtProducto: TextView = v.findViewById(R.id.txtProductoEmpeno)
        val txtValor: TextView = v.findViewById(R.id.txtValorEmpeno)
        val txtPuntos: TextView = v.findViewById(R.id.txtPuntosEmpeno)
        val txtEstado: TextView = v.findViewById(R.id.txtEstadoEmpeno)
        val txtFecha: TextView = v.findViewById(R.id.txtFechaEmpeno)
        val imgFoto1: ImageView = v.findViewById(R.id.imgFoto1Empeno)
        val imgFoto2: ImageView = v.findViewById(R.id.imgFoto2Empeno)
        val btnAprobar: Button = v.findViewById(R.id.btnAprobarEmpeno)
        val btnRechazar: Button = v.findViewById(R.id.btnRechazarEmpeno)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_empeno_pendiente, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val empeno = items[position]

        holder.txtProducto.text = empeno.producto
        holder.txtValor.text = "Valor: S/ ${empeno.valor}"
        holder.txtPuntos.text = "Puntos a otorgar: ${empeno.puntos} (S/ ${empeno.puntos * 0.5})"
        holder.txtEstado.text = "Estado: ${empeno.estado}"
        holder.txtFecha.text = "Fecha: ${empeno.fecha}"

        if (empeno.foto1Url.isNotEmpty()) {
            try {
                val cleanBase64 = empeno.foto1Url.replace("\n", "").replace("\r", "")
                val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap != null) {
                    holder.imgFoto1.setImageBitmap(bitmap)
                    holder.imgFoto1.visibility = View.VISIBLE
                } else {
                    holder.imgFoto1.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                holder.imgFoto1.visibility = View.GONE
            }
        } else {
            holder.imgFoto1.visibility = View.GONE
        }

        if (empeno.foto2Url.isNotEmpty()) {
            try {
                val cleanBase64 = empeno.foto2Url.replace("\n", "").replace("\r", "")
                val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap != null) {
                    holder.imgFoto2.setImageBitmap(bitmap)
                    holder.imgFoto2.visibility = View.VISIBLE
                } else {
                    holder.imgFoto2.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                holder.imgFoto2.visibility = View.GONE
            }
        } else {
            holder.imgFoto2.visibility = View.GONE
        }

        holder.btnAprobar.setOnClickListener { onAprobar(empeno) }
        holder.btnRechazar.setOnClickListener { onRechazar(empeno) }
    }

    override fun getItemCount() = items.size
}
