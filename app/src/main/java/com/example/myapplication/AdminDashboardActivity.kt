package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.app.AlertDialog

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var tvBienvenidaAdmin: TextView
    private lateinit var tvEstadisticas: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val CAMERA_REQUEST_CODE = 100
    private val CAMERA_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        tvBienvenidaAdmin = findViewById(R.id.textoBienvenidaAdmin)
        tvEstadisticas = findViewById(R.id.textoEstadisticas)

        auth.currentUser?.let { currentUser ->
            val uid = currentUser.uid
            firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    val nombre = document.getString("nombre")
                    tvBienvenidaAdmin.text = if (!nombre.isNullOrEmpty()) {
                        "¡Bienvenido Administrador, $nombre!"
                    } else {
                        "¡Bienvenido Administrador!"
                    }
                }
                .addOnFailureListener {
                    tvBienvenidaAdmin.text = "¡Bienvenido Administrador!"
                }
        } ?: run {
            tvBienvenidaAdmin.text = "¡Bienvenido Administrador!"
        }

        cargarEstadisticas()

        val btnGestionUsuarios = findViewById<Button>(R.id.btnGestionUsuarios)
        val btnGestionEmpenos = findViewById<Button>(R.id.btnGestionEmpenos)
        val btnGestionCupones = findViewById<Button>(R.id.btnGestionCupones)
        val btnReportes = findViewById<Button>(R.id.btnReportes)
        val btnConfiguracion = findViewById<Button>(R.id.btnConfiguracion)
        val btnCerrarSesionAdmin = findViewById<Button>(R.id.btnCerrarSesionAdmin)
        val btnIAReconocimiento = findViewById<Button>(R.id.btnIAReconocimiento)
        val btnGestionPagos = findViewById<Button>(R.id.btnGestionPagos)
        val btnGestionReparaciones = findViewById<Button>(R.id.btnGestionReparaciones)

        btnGestionUsuarios.setOnClickListener {
            startActivity(Intent(this, AdminUsuariosActivity::class.java))
        }

        btnGestionEmpenos.setOnClickListener {
            startActivity(Intent(this, AdminEmpenosActivity::class.java))
        }

        btnGestionCupones.setOnClickListener {
            startActivity(Intent(this, AdminCuponesActivity::class.java))
        }

        btnReportes.setOnClickListener {
            startActivity(Intent(this, AdminReportesActivity::class.java))
        }

        btnConfiguracion.setOnClickListener {
            startActivity(Intent(this, AdminConfiguracionActivity::class.java))
        }

        btnIAReconocimiento.setOnClickListener {
            if (checkCameraPermission()) {
                abrirCamara()
            } else {
                solicitarPermisoCamera()
            }
        }

        btnGestionPagos.setOnClickListener {
            startActivity(Intent(this, AdminPagosActivity::class.java))
        }

        btnGestionReparaciones.setOnClickListener {
            startActivity(Intent(this, AdminGestionReparacionesActivity::class.java))
        }

        btnCerrarSesionAdmin.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, AdminLoginActivity::class.java))
            finish()
        }
    }

    private fun cargarEstadisticas() {
        firestore.collection("usuarios").get()
            .addOnSuccessListener { usuariosSnapshot ->
                firestore.collection("ordenes_reparacion").get()
                    .addOnSuccessListener { ordenesSnapshot ->
                        firestore.collection("empenos").get()
                            .addOnSuccessListener { empenosSnapshot ->
                                val estadisticas = """
                                    Usuarios Registrados: ${usuariosSnapshot.size()}
                                    Órdenes de Servicio: ${ordenesSnapshot.size()}
                                    Empeños Activos: ${empenosSnapshot.size()}
                                """.trimIndent()
                                tvEstadisticas.text = estadisticas
                            }
                    }
            }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun solicitarPermisoCamera() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val bitmap = data?.getParcelableExtra<Bitmap>("data")
            if (bitmap != null) {
                reconocerLaptop(bitmap)
            }
        }
    }

    private fun reconocerLaptop(fotoCaptured: Bitmap) {
        Toast.makeText(this, "Analizando equipo...", Toast.LENGTH_SHORT).show()

        firestore.collection("ordenes_reparacion").get()
            .addOnSuccessListener { snapshot ->
                println("[v0] Total de documentos: ${snapshot.size()}")

                var mejorCoincidencia: Pair<String, Float>? = null
                var mejorCliente = ""

                for (document in snapshot.documents) {
                    val nombreCliente = document.getString("nombre") ?: "Desconocido"
                    val imagenesArray = document.get("imagenes") as? List<String>

                    println("[v0] Cliente: $nombreCliente, Imágenes encontradas: ${imagenesArray?.size ?: 0}")

                    if (imagenesArray != null && imagenesArray.isNotEmpty()) {
                        for ((index, imagenBase64) in imagenesArray.withIndex()) {
                            if (imagenBase64.isNotEmpty()) {
                                try {
                                    val confianza = compararImagenesAvanzado(fotoCaptured, imagenBase64)
                                    println("[v0] Cliente: $nombreCliente, Imagen $index - Confianza: $confianza")

                                    if (mejorCoincidencia == null || confianza > mejorCoincidencia.second) {
                                        mejorCoincidencia = Pair(nombreCliente, confianza)
                                        mejorCliente = nombreCliente
                                    }
                                } catch (e: Exception) {
                                    println("[v0] Error comparando imagen: ${e.message}")
                                }
                            }
                        }
                    }
                }

                println("[v0] Mejor coincidencia: ${mejorCoincidencia?.first} con confianza ${mejorCoincidencia?.second}")

                if (mejorCoincidencia != null && mejorCoincidencia.second > 0.50f) {
                    mostrarDialogoExito(mejorCliente)
                } else {
                    mostrarDialogoError()
                }
            }
            .addOnFailureListener { exception ->
                println("[v0] Error al cargar datos: ${exception.message}")
                Toast.makeText(this, "Error al cargar datos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun compararImagenesAvanzado(bitmap1: Bitmap, imagenBase64: String): Float {
        return try {
            val decodedBytes = Base64.decode(imagenBase64, Base64.DEFAULT)
            val bitmap2 = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            val width = 100
            val height = 100
            val resized1 = Bitmap.createScaledBitmap(bitmap1, width, height, true)
            val resized2 = Bitmap.createScaledBitmap(bitmap2, width, height, true)

            var pixelesIguales = 0
            val pixeles1 = IntArray(width * height)
            val pixeles2 = IntArray(width * height)

            resized1.getPixels(pixeles1, 0, width, 0, 0, width, height)
            resized2.getPixels(pixeles2, 0, width, 0, 0, width, height)

            for (i in pixeles1.indices) {
                val r1 = (pixeles1[i] shr 16) and 0xFF
                val g1 = (pixeles1[i] shr 8) and 0xFF
                val b1 = pixeles1[i] and 0xFF

                val r2 = (pixeles2[i] shr 16) and 0xFF
                val g2 = (pixeles2[i] shr 8) and 0xFF
                val b2 = pixeles2[i] and 0xFF

                // Considerar píxeles similares si la diferencia es menor a 30
                if (Math.abs(r1 - r2) < 30 && Math.abs(g1 - g2) < 30 && Math.abs(b1 - b2) < 30) {
                    pixelesIguales++
                }
            }

            val similitud = pixelesIguales.toFloat() / (width * height)
            println("[v0] Similitud calculada: $similitud")
            similitud
        } catch (e: Exception) {
            println("[v0] Error en comparación: ${e.message}")
            0f
        }
    }

    private fun mostrarDialogoExito(cliente: String) {
        val mensaje = "Este equipo pertenece a:\n\n$cliente"

        AlertDialog.Builder(this)
            .setTitle("✓ EQUIPO IDENTIFICADO")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { _, _ ->
                Toast.makeText(this, "✓ Equipo de $cliente confirmado", Toast.LENGTH_SHORT).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun mostrarDialogoError() {
        AlertDialog.Builder(this)
            .setTitle("⚠️ EQUIPO NO REGISTRADO")
            .setMessage("El equipo capturado no está registrado en el sistema.\n\nIntente nuevamente con un enfoque diferente.")
            .setPositiveButton("REINTENTAR") { _, _ ->
                if (checkCameraPermission()) {
                    abrirCamara()
                } else {
                    solicitarPermisoCamera()
                }
            }
            .setNegativeButton("CANCELAR") { _, _ -> }
            .setCancelable(false)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
