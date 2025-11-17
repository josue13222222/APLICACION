package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SeguimientoActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var containerEstado: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seguimiento)

        val btnBuscar = findViewById<Button>(R.id.btnBuscar)
        val etNumeroOrden = findViewById<EditText>(R.id.etNumeroOrden)
        containerEstado = findViewById(R.id.containerEstado)

        btnBuscar.setOnClickListener {
            val numeroOrden = etNumeroOrden.text.toString().trim()
            if (numeroOrden.isNotEmpty()) {
                buscarEstado(numeroOrden)
            } else {
                Toast.makeText(this, "Por favor ingrese un número de orden.", Toast.LENGTH_SHORT).show()
            }
        }

        val btnContacto = findViewById<Button>(R.id.btnContactoSoporte)
        btnContacto.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:+57315245863")
            startActivity(intent)
        }
    }

    private fun buscarEstado(numeroOrden: String) {
        db.collection("ordenes_reparacion")
            .document(numeroOrden)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    mostrarDetalleOrden(document.data ?: emptyMap())
                } else {
                    Toast.makeText(this, "Orden no encontrada.", Toast.LENGTH_SHORT).show()
                    containerEstado.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al consultar la orden.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDetalleOrden(ordenData: Map<String, Any>) {
        val nombre = ordenData["nombre"]?.toString() ?: "No especificado"
        val telefono = ordenData["telefono"]?.toString() ?: "No disponible"
        val equipo = ordenData["equipo"]?.toString() ?: "No especificado"
        val problema = ordenData["problema"]?.toString() ?: "No especificado"
        val estado = ordenData["estado"]?.toString() ?: "En Reparación"

        // Actualizar información del cliente
        findViewById<TextView>(R.id.tvNombreCliente).text = "👤 Nombre: $nombre"
        findViewById<TextView>(R.id.tvTelefonoCliente).text = "📱 Teléfono: $telefono"
        findViewById<TextView>(R.id.tvEquipo).text = "💻 Equipo: $equipo"
        findViewById<TextView>(R.id.tvProblema).text = "⚠️ Problema: $problema"
        findViewById<TextView>(R.id.tvEstadoActual).text = estado

        val (porcentaje, estadoActivo) = actualizarProgresoNuevo(estado)
        findViewById<ProgressBar>(R.id.pbEstado).progress = porcentaje
        findViewById<TextView>(R.id.tvPorcentajeProgreso).text = "$porcentaje%"

        // Actualizar etapas con los nuevos estados
        actualizarEtapasNuevas(estadoActivo)

        containerEstado.visibility = View.VISIBLE
    }

    private fun actualizarProgresoNuevo(estado: String): Pair<Int, String> {
        val (porcentaje, estadoActual) = when (estado.lowercase()) {
            "en reparación" -> Pair(50, "en_reparacion")
            "listo para recoger" -> Pair(100, "listo_recoger")
            else -> Pair(50, "en_reparacion") // Por defecto en reparación
        }
        return Pair(porcentaje, estadoActual)
    }

    private fun actualizarEtapasNuevas(estadoActivo: String) {
        // Etapa 1: En Reparación
        val tvStatus1 = findViewById<TextView>(R.id.tvEtapa1Status)
        val tvFecha1 = findViewById<TextView>(R.id.tvEtapa1Fecha)

        if (estadoActivo == "en_reparacion" || estadoActivo == "listo_recoger") {
            tvStatus1?.text = "✓"
            tvStatus1?.setTextColor(resources.getColor(android.R.color.holo_orange_light))
            tvFecha1?.text = "Fecha: ${obtenerFechaActual()}"
        } else {
            tvStatus1?.text = "○"
            tvStatus1?.setTextColor(resources.getColor(android.R.color.darker_gray))
            tvFecha1?.text = "Fecha: Pendiente"
        }

        // Etapa 2: Listo para Recoger
        val tvStatus2 = findViewById<TextView>(R.id.tvEtapa2Status)
        val tvFecha2 = findViewById<TextView>(R.id.tvEtapa2Fecha)

        if (estadoActivo == "listo_recoger") {
            tvStatus2?.text = "✓"
            tvStatus2?.setTextColor(resources.getColor(android.R.color.holo_green_light))
            tvFecha2?.text = "Fecha: ${obtenerFechaActual()}"
        } else {
            tvStatus2?.text = "○"
            tvStatus2?.setTextColor(resources.getColor(android.R.color.darker_gray))
            tvFecha2?.text = "Fecha: Pendiente"
        }
    }

    private fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
}
