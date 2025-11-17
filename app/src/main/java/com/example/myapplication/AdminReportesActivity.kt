package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.kernel.font.PdfFontFactory

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
    private lateinit var txtPuntosTotales: TextView
    private lateinit var txtPuntosUsados: TextView
    private lateinit var btnGenerarReporte: Button
    private lateinit var btnExportarPDF: Button
    private lateinit var scrollViewReporte: ScrollView

    private var totalUsuarios = 0
    private var usuariosActivos = 0
    private var totalEmpenos = 0
    private var empenosAprobados = 0
    private var empenosPendientes = 0
    private var totalCupones = 0
    private var cuponesUsados = 0
    private var puntosTotales = 0
    private var puntosUsados = 0
    private var ingresosMes = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_reportes)

        db = FirebaseFirestore.getInstance()
        initViews()
        cargarEstadisticas()

        btnGenerarReporte.setOnClickListener {
            generarReporteCompleto()
        }
        btnExportarPDF.setOnClickListener {
            exportarReportePDF()
        }
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
        txtPuntosTotales = findViewById(R.id.txtPuntosTotales)
        txtPuntosUsados = findViewById(R.id.txtPuntosUsados)
        btnGenerarReporte = findViewById(R.id.btnGenerarReporte)
        btnExportarPDF = findViewById(R.id.btnExportarPDF)
        scrollViewReporte = findViewById(R.id.scrollViewReporte)
    }

    private fun cargarEstadisticas() {
        cargarEstadisticasUsuariosRealTime()
        cargarEstadisticasEmpenosRealTime()
        cargarEstadisticasCuponesRealTime()
        cargarIngresosRealTime()
    }

    private fun cargarEstadisticasUsuariosRealTime() {
        db.collection("usuarios").addSnapshotListener { documents, exception ->
            if (exception != null) {
                Log.d("AdminReportes", "Error al cargar usuarios: ${exception.message}")
                txtTotalUsuarios.text = "0"
                txtUsuariosActivos.text = "0"
                totalUsuarios = 0
                usuariosActivos = 0
                return@addSnapshotListener
            }

            if (documents != null) {
                val total = documents.size()
                var activos = 0

                for (document in documents) {
                    val activo = document.getBoolean("activo") ?: true
                    if (activo) activos++
                }

                totalUsuarios = total
                usuariosActivos = activos
                txtTotalUsuarios.text = total.toString()
                txtUsuariosActivos.text = activos.toString()
                Log.d("AdminReportes", "Usuarios cargados: Total=$total, Activos=$activos")
            }
        }
    }

    private fun cargarEstadisticasEmpenosRealTime() {
        db.collection("empenos").addSnapshotListener { documents, exception ->
            if (exception != null) {
                Log.d("AdminReportes", "Error al cargar empeños: ${exception.message}")
                txtTotalEmpenos.text = "0"
                txtEmpenosAprobados.text = "0"
                txtEmpenosPendientes.text = "0"
                return@addSnapshotListener
            }

            if (documents != null) {
                val total = documents.size()
                var aprobados = 0
                var pendientes = 0

                for (document in documents) {
                    when (document.getString("estado")) {
                        "aprobado" -> aprobados++
                        "pendiente" -> pendientes++
                    }
                }

                totalEmpenos = total
                empenosAprobados = aprobados
                empenosPendientes = pendientes
                txtTotalEmpenos.text = total.toString()
                txtEmpenosAprobados.text = aprobados.toString()
                txtEmpenosPendientes.text = pendientes.toString()
                Log.d("AdminReportes", "Empeños cargados: Total=$total, Aprobados=$aprobados, Pendientes=$pendientes")
            }
        }
    }

    private fun cargarEstadisticasCuponesRealTime() {
        db.collection("cupones").addSnapshotListener { documents, exception ->
            if (exception != null) {
                Log.d("AdminReportes", "Error al cargar cupones: ${exception.message}")
                txtTotalCupones.text = "0"
                txtCuponesUsados.text = "0"
                txtPuntosTotales.text = "0"
                txtPuntosUsados.text = "0"
                return@addSnapshotListener
            }

            if (documents != null) {
                val total = documents.size()
                var usados = 0
                var ptTotales = 0
                var ptUsados = 0

                for (document in documents) {
                    val puntos = document.getLong("puntos")?.toInt() ?: 0
                    val puntosUso = document.getLong("puntos_usados")?.toInt() ?: 0
                    val usos = document.getLong("usos")?.toInt() ?: 0

                    ptTotales += puntos
                    ptUsados += puntosUso

                    if (usos > 0) usados++
                }

                totalCupones = total
                cuponesUsados = usados
                puntosTotales = ptTotales
                puntosUsados = ptUsados

                txtTotalCupones.text = total.toString()
                txtCuponesUsados.text = usados.toString()
                txtPuntosTotales.text = ptTotales.toString()
                txtPuntosUsados.text = ptUsados.toString()
                Log.d("AdminReportes", "Cupones cargados: Total=$total, Usados=$usados, Puntos=$ptTotales, Puntos Usados=$ptUsados")
            }
        }
    }

    private fun cargarIngresosRealTime() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val inicioMes = com.google.firebase.Timestamp(calendar.time)

        db.collection("transacciones")
            .whereGreaterThanOrEqualTo("fecha", inicioMes)
            .addSnapshotListener { documents, exception ->
                if (exception != null) {
                    Log.d("AdminReportes", "Error al cargar transacciones: ${exception.message}")
                    txtIngresosMes.text = "$0.00"
                    ingresosMes = 0.0
                    return@addSnapshotListener
                }

                if (documents != null) {
                    var totalIngresos = 0.0
                    for (document in documents) {
                        val monto = document.getDouble("monto") ?: 0.0
                        totalIngresos += monto
                    }
                    ingresosMes = totalIngresos
                    txtIngresosMes.text = String.format("$%.2f", totalIngresos)
                    Log.d("AdminReportes", "Ingresos cargados: $totalIngresos")
                }
            }
    }

    private fun generarReporteCompleto() {
        val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        val reporte = StringBuilder()
        reporte.append("=== REPORTE ADMINISTRATIVO COMPLETO ===\n")
        reporte.append("Fecha de Generación: $fechaActual\n")
        reporte.append("=".repeat(40) + "\n\n")

        reporte.append("📊 SECCIÓN DE USUARIOS:\n")
        reporte.append("-".repeat(40) + "\n")
        reporte.append("• Total de Usuarios Registrados: $totalUsuarios\n")
        reporte.append("• Usuarios Activos: $usuariosActivos\n")
        val bloqueados = totalUsuarios - usuariosActivos
        reporte.append("• Usuarios Bloqueados: $bloqueados\n\n")

        reporte.append("💎 SECCIÓN DE EMPEÑOS:\n")
        reporte.append("-".repeat(40) + "\n")
        reporte.append("• Total de Empeños Registrados: $totalEmpenos\n")
        reporte.append("• Empeños Aprobados: $empenosAprobados\n")
        reporte.append("• Empeños Pendientes: $empenosPendientes\n\n")

        reporte.append("🎫 SECCIÓN DE CUPONES:\n")
        reporte.append("-".repeat(40) + "\n")
        reporte.append("• Total de Cupones en Sistema: $totalCupones\n")
        reporte.append("• Cupones Utilizados: $cuponesUsados\n")
        reporte.append("• Puntos Totales Disponibles: $puntosTotales\n")
        reporte.append("• Puntos Utilizados: $puntosUsados\n")
        reporte.append("• Puntos Disponibles: ${puntosTotales - puntosUsados}\n\n")

        reporte.append("💰 SECCIÓN DE FINANZAS:\n")
        reporte.append("-".repeat(40) + "\n")
        reporte.append("• Ingresos del Mes Actual: $${String.format("%.2f", ingresosMes)}\n\n")

        reporte.append("=".repeat(40) + "\n")
        reporte.append("=== FIN DEL REPORTE ===")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📄 Reporte Administrativo")
            .setMessage(reporte.toString())
            .setPositiveButton("Aceptar") { _, _ -> }
            .setNegativeButton("Copiar") { _, _ ->
                val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Reporte", reporte.toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Reporte copiado al portapapeles", Toast.LENGTH_SHORT).show()
            }
            .show()

        Toast.makeText(this, "✓ Reporte generado exitosamente", Toast.LENGTH_LONG).show()
    }

    private fun exportarReportePDF() {
        try {
            val fechaActual = SimpleDateFormat("dd_MM_yyyy_HH_mm", Locale.getDefault()).format(Date())
            val archivo = File(
                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "Reporte_$fechaActual.pdf"
            )

            val writer = PdfWriter(archivo.absolutePath)
            val pdfDoc = PdfDocument(writer)
            val document = Document(pdfDoc)

            val titulo = Paragraph("REPORTE ADMINISTRATIVO COMPLETO")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18f)
                .setBold()

            val fecha = Paragraph("Fecha: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(11f)

            document.add(titulo)
            document.add(fecha)
            document.add(Paragraph("\n"))

            // Sección Usuarios
            document.add(Paragraph("SECCIÓN DE USUARIOS").setFontSize(14f).setBold())
            val tableUsuarios = Table(2)
            tableUsuarios.addCell("Total Usuarios")
            tableUsuarios.addCell(totalUsuarios.toString())
            tableUsuarios.addCell("Usuarios Activos")
            tableUsuarios.addCell(usuariosActivos.toString())
            tableUsuarios.addCell("Usuarios Bloqueados")
            tableUsuarios.addCell((totalUsuarios - usuariosActivos).toString())
            document.add(tableUsuarios)
            document.add(Paragraph("\n"))

            // Sección Empeños
            document.add(Paragraph("SECCIÓN DE EMPEÑOS").setFontSize(14f).setBold())
            val tableEmpenos = Table(2)
            tableEmpenos.addCell("Total Empeños")
            tableEmpenos.addCell(totalEmpenos.toString())
            tableEmpenos.addCell("Empeños Aprobados")
            tableEmpenos.addCell(empenosAprobados.toString())
            tableEmpenos.addCell("Empeños Pendientes")
            tableEmpenos.addCell(empenosPendientes.toString())
            document.add(tableEmpenos)
            document.add(Paragraph("\n"))

            // Sección Cupones
            document.add(Paragraph("SECCIÓN DE CUPONES").setFontSize(14f).setBold())
            val tableCupones = Table(2)
            tableCupones.addCell("Total Cupones")
            tableCupones.addCell(totalCupones.toString())
            tableCupones.addCell("Cupones Usados")
            tableCupones.addCell(cuponesUsados.toString())
            tableCupones.addCell("Puntos Totales")
            tableCupones.addCell(puntosTotales.toString())
            tableCupones.addCell("Puntos Utilizados")
            tableCupones.addCell(puntosUsados.toString())
            tableCupones.addCell("Puntos Disponibles")
            tableCupones.addCell((puntosTotales - puntosUsados).toString())
            document.add(tableCupones)
            document.add(Paragraph("\n"))

            // Sección Finanzas
            document.add(Paragraph("SECCIÓN DE FINANZAS").setFontSize(14f).setBold())
            val tableFinanzas = Table(2)
            tableFinanzas.addCell("Ingresos del Mes")
            tableFinanzas.addCell(String.format("$%.2f", ingresosMes))
            document.add(tableFinanzas)

            document.close()

            Toast.makeText(this, "PDF generado exitosamente: ${archivo.absolutePath}", Toast.LENGTH_LONG).show()

            // Abrir el PDF
            abrirPDF(archivo)

        } catch (e: Exception) {
            Log.e("AdminReportes", "Error al generar PDF: ${e.message}")
            Toast.makeText(this, "Error al generar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirPDF(archivo: File) {
        try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(this, "${packageName}.fileprovider", archivo)
            } else {
                Uri.fromFile(archivo)
            }

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("AdminReportes", "Error al abrir PDF: ${e.message}")
            Toast.makeText(this, "No hay aplicación para abrir PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
