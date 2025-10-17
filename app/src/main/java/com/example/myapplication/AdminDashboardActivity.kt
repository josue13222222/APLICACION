package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Referencias a vistas
        tvBienvenidaAdmin = findViewById(R.id.textoBienvenidaAdmin)
        tvEstadisticas = findViewById(R.id.textoEstadisticas)

        // Mostrar datos del administrador
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

        // Cargar estadísticas básicas
        cargarEstadisticas()

        // Botones del panel admin
        val btnGestionUsuarios = findViewById<Button>(R.id.btnGestionUsuarios)
        val btnGestionEmpenos = findViewById<Button>(R.id.btnGestionEmpenos)
        val btnGestionCupones = findViewById<Button>(R.id.btnGestionCupones)
        val btnReportes = findViewById<Button>(R.id.btnReportes)
        val btnConfiguracion = findViewById<Button>(R.id.btnConfiguracion)
        val btnCerrarSesionAdmin = findViewById<Button>(R.id.btnCerrarSesionAdmin)
        val btnIAReconocimiento = findViewById<Button>(R.id.btnIAReconocimiento)
        val btnGestionPagos = findViewById<Button>(R.id.btnGestionPagos)
        val btnGestionReparaciones = findViewById<Button>(R.id.btnGestionReparaciones)

        // Funcionalidad de botones
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
            startActivity(Intent(this, AdminPanelActivity::class.java))
        }

        // Cerrar sesión admin
        btnCerrarSesionAdmin.setOnClickListener {
            val sharedPreferences = getSharedPreferences("AdminLoginPrefs", MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean("IS_ADMIN_LOGGED_IN", false)
                apply()
            }

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun solicitarPermisoCamera() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        } else {
            Toast.makeText(this, "No se puede acceder a la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                procesarImagenConIA(it)
            }
        }
    }

    private fun procesarImagenConIA(bitmap: Bitmap) {
        Toast.makeText(this, "🤖 IA Analizando imagen...", Toast.LENGTH_SHORT).show()

        // Simular procesamiento de IA (en una implementación real usarías una API de IA)
        val laptopModels = arrayOf(
            "Dell Inspiron 15", "HP Pavilion", "Lenovo ThinkPad",
            "MacBook Air", "ASUS VivoBook", "Acer Aspire"
        )

        // Simular resultado aleatorio
        val modeloDetectado = laptopModels.random()
        val confianza = (85..98).random()

        // Mostrar resultado
        val mensaje = "🔍 Laptop detectada:\n" +
                "Modelo: $modeloDetectado\n" +
                "Confianza: $confianza%\n" +
                "Estado: Listo para servicio técnico"

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()

        // Aquí podrías guardar el resultado en Firebase o mostrar en una nueva actividad
    }

    private fun cargarEstadisticas() {
        // Contar usuarios totales
        firestore.collection("usuarios")
            .whereEqualTo("isAdmin", false)
            .get()
            .addOnSuccessListener { documents ->
                val totalUsuarios = documents.size()

                firestore.collection("ordenes_reparacion")
                    .get()
                    .addOnSuccessListener { reparacionesDoc ->
                        val totalReparaciones = reparacionesDoc.size()
                        val reparacionesActivas = reparacionesDoc.documents.count {
                            val estado = it.getString("estado")
                            estado != "Entregado"
                        }

                        // Contar empeños activos
                        firestore.collection("empenos")
                            .whereEqualTo("estado", "Aprobado")
                            .get()
                            .addOnSuccessListener { empeñosDoc ->
                                val totalEmpeños = empeñosDoc.size()

                                tvEstadisticas.text = "Usuarios: $totalUsuarios\n" +
                                        "Empeños activos: $totalEmpeños\n" +
                                        "Reparaciones activas: $reparacionesActivas/$totalReparaciones"
                            }
                            .addOnFailureListener {
                                tvEstadisticas.text = "Usuarios: $totalUsuarios\n" +
                                        "Reparaciones activas: $reparacionesActivas/$totalReparaciones"
                            }
                    }
                    .addOnFailureListener {
                        tvEstadisticas.text = "Usuarios registrados: $totalUsuarios\nEstadísticas no disponibles"
                    }
            }
            .addOnFailureListener {
                tvEstadisticas.text = "Estadísticas no disponibles"
            }
    }
}
