package com.example.myapplication

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class AdminReportesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var txtTotalUsuarios: TextView
    private lateinit var txtTotalEmpenos: TextView
    private lateinit var txtTotalCupones: TextView
    private lateinit var txtIngresosMes: TextView
    private lateinit var txtEmpenosAprobados: TextView
    private lateinit var txtEmpenosPendientes: TextView
    private lateinit var txtUsuariosActivos: TextView
    private lateinit var txtCuponesUsados: TextView
    private lateinit var btnGenerarReporte: Button
    private lateinit var scrollViewReporte: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_reportes)

        db = FirebaseFirestore.getInstance()
        initViews()
        cargarEstadisticas()

        btnGenerarReporte.setOnClickListener { generarReporteCompleto() }
    }

    private fun initViews() {
        txtTotalUsuarios = findViewById(R.id.txtTotalUsuarios)
        txtTotalEmpenos = findViewById(R.id.txtTotalEmpenos)
        txtTotalCupones = findViewById(R.id.txtTotalCupones)
        txtIngresosMes = findViewById(R.id.txtIngresosMes)
        txtEmpenosAprobados = findViewById(R.id.txtEmpenosAprobados)
        txtEmpenosPendientes = findViewById(R.id.txtEmpenosPendientes)
        txtUsuariosActivos = findViewById(R.id.txtUsuariosActivos)
        txtCuponesUsados = findViewById(R.id.txtCuponesUsados)
        btnGenerarReporte = findViewById(R.id.btnGenerarReporte)
        scrollViewReporte = findViewById(R.id.scrollViewReporte)
    }

    private fun cargarEstadisticas() {
        cargarEstadisticasUsuarios()
        cargarEstadisticasEmpenos()
        cargarEstadisticasCupones()
        cargarIngresosMes()
    }

    private fun cargarEstadisticasUsuarios() {
        db.collection("usuarios").get()
            .addOnSuccessListener { documents ->
                val total = documents.size()
                var activos = 0

                for (document in documents) {
                    val activo = document.getBoolean("activo") ?: true
                    if (activo) activos++
                }

                txtTotalUsuarios.text = total.toString()
                txtUsuariosActivos.text = activos.toString()
            }
    }

    private fun cargarEstadisticasEmpenos() {
        db.collection("empenos").get()
            .addOnSuccessListener { documents ->
                val total = documents.size()
                var aprobados = 0
                var pendientes = 0

                for (document in documents) {
                    when (document.getString("estado")) {
                        "aprobado" -> aprobados++
                        "pendiente" -> pendientes++
                    }
                }

                txtTotalEmpenos.text = total.toString()
                txtEmpenosAprobados.text = aprobados.toString()
                txtEmpenosPendientes.text = pendientes.toString()
            }
    }

    private fun cargarEstadisticasCupones() {
        db.collection("cupones").get()
            .addOnSuccessListener { documents ->
                val total = documents.size()
                var usados = 0

                for (document in documents) {
                    val usos = document.getLong("usos")?.toInt() ?: 0
                    if (usos > 0) usados++
                }

                txtTotalCupones.text = total.toString()
                txtCuponesUsados.text = usados.toString()
            }
    }

    private fun cargarIngresosMes() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val inicioMes = com.google.firebase.Timestamp(calendar.time)

        db.collection("transacciones")
            .whereGreaterThanOrEqualTo("fecha", inicioMes)
            .get()
            .addOnSuccessListener { documents ->
                var totalIngresos = 0.0
                for (document in documents) {
                    val monto = document.getDouble("monto") ?: 0.0
                    totalIngresos += monto
                }
                txtIngresosMes.text = String.format("$%.2f", totalIngresos)
            }
            .addOnFailureListener {
                txtIngresosMes.text = "$0.00"
            }
    }

    private fun generarReporteCompleto() {
        val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        val reporte = StringBuilder()
        reporte.append("=== REPORTE ADMINISTRATIVO ===\n")
        reporte.append("Fecha: $fechaActual\n\n")

        reporte.append("USUARIOS:\n")
        reporte.append("- Total: ${txtTotalUsuarios.text}\n")
        reporte.append("- Activos: ${txtUsuariosActivos.text}\n")
        reporte.append("- Bloqueados: ${txtTotalUsuarios.text.toString().toInt() - txtUsuariosActivos.text.toString().toInt()}\n\n")

        reporte.append("EMPEÑOS:\n")
        reporte.append("- Total: ${txtTotalEmpenos.text}\n")
        reporte.append("- Aprobados: ${txtEmpenosAprobados.text}\n")
        reporte.append("- Pendientes: ${txtEmpenosPendientes.text}\n\n")

        reporte.append("CUPONES:\n")
        reporte.append("- Total: ${txtTotalCupones.text}\n")
        reporte.append("- Usados: ${txtCuponesUsados.text}\n\n")

        reporte.append("FINANZAS:\n")
        reporte.append("- Ingresos del mes: ${txtIngresosMes.text}\n\n")

        reporte.append("=== FIN DEL REPORTE ===")

        Toast.makeText(this, "Reporte generado exitosamente", Toast.LENGTH_LONG).show()

        // Aquí podrías implementar funcionalidad para guardar o compartir el reporte
        // Por ejemplo, guardarlo en un archivo o enviarlo por email
    }
}
