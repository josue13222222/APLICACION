package com.example.myapplication

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AdminConfiguracionActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var etNombreApp: EditText
    private lateinit var etVersionApp: EditText
    private lateinit var etEmailSoporte: EditText
    private lateinit var etTelefonoSoporte: EditText
    private lateinit var switchMantenimiento: Switch
    private lateinit var switchRegistroUsuarios: Switch
    private lateinit var etMensajeMantenimiento: EditText
    private lateinit var btnGuardarConfig: Button
    private lateinit var btnRestablecerConfig: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_configuracion)

        db = FirebaseFirestore.getInstance()
        initViews()
        cargarConfiguracion()

        btnGuardarConfig.setOnClickListener { guardarConfiguracion() }
        btnRestablecerConfig.setOnClickListener { restablecerConfiguracion() }
    }

    private fun initViews() {
        etNombreApp = findViewById(R.id.etNombreApp)
        etVersionApp = findViewById(R.id.etVersionApp)
        etEmailSoporte = findViewById(R.id.etEmailSoporte)
        etTelefonoSoporte = findViewById(R.id.etTelefonoSoporte)
        switchMantenimiento = findViewById(R.id.switchMantenimiento)
        switchRegistroUsuarios = findViewById(R.id.switchRegistroUsuarios)
        etMensajeMantenimiento = findViewById(R.id.etMensajeMantenimiento)
        btnGuardarConfig = findViewById(R.id.btnGuardarConfig)
        btnRestablecerConfig = findViewById(R.id.btnRestablecerConfig)
    }

    private fun cargarConfiguracion() {
        db.collection("configuracion").document("app")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    etNombreApp.setText(document.getString("nombreApp") ?: "Robot")
                    etVersionApp.setText(document.getString("versionApp") ?: "1.0.0")
                    etEmailSoporte.setText(document.getString("emailSoporte") ?: "soporte@robot.com")
                    etTelefonoSoporte.setText(document.getString("telefonoSoporte") ?: "+1234567890")
                    switchMantenimiento.isChecked = document.getBoolean("modoMantenimiento") ?: false
                    switchRegistroUsuarios.isChecked = document.getBoolean("permitirRegistro") ?: true
                    etMensajeMantenimiento.setText(document.getString("mensajeMantenimiento") ?: "La aplicación está en mantenimiento. Intenta más tarde.")
                } else {
                    // Valores por defecto
                    establecerValoresPorDefecto()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar configuración: ${exception.message}", Toast.LENGTH_SHORT).show()
                establecerValoresPorDefecto()
            }
    }

    private fun establecerValoresPorDefecto() {
        etNombreApp.setText("Robot")
        etVersionApp.setText("1.0.0")
        etEmailSoporte.setText("soporte@robot.com")
        etTelefonoSoporte.setText("+1234567890")
        switchMantenimiento.isChecked = false
        switchRegistroUsuarios.isChecked = true
        etMensajeMantenimiento.setText("La aplicación está en mantenimiento. Intenta más tarde.")
    }

    private fun guardarConfiguracion() {
        val nombreApp = etNombreApp.text.toString().trim()
        val versionApp = etVersionApp.text.toString().trim()
        val emailSoporte = etEmailSoporte.text.toString().trim()
        val telefonoSoporte = etTelefonoSoporte.text.toString().trim()
        val mensajeMantenimiento = etMensajeMantenimiento.text.toString().trim()

        if (nombreApp.isEmpty() || versionApp.isEmpty() || emailSoporte.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val configuracion = hashMapOf(
            "nombreApp" to nombreApp,
            "versionApp" to versionApp,
            "emailSoporte" to emailSoporte,
            "telefonoSoporte" to telefonoSoporte,
            "modoMantenimiento" to switchMantenimiento.isChecked,
            "permitirRegistro" to switchRegistroUsuarios.isChecked,
            "mensajeMantenimiento" to mensajeMantenimiento,
            "fechaActualizacion" to com.google.firebase.Timestamp.now()
        )

        db.collection("configuracion").document("app")
            .set(configuracion)
            .addOnSuccessListener {
                Toast.makeText(this, "Configuración guardada exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al guardar configuración: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun restablecerConfiguracion() {
        establecerValoresPorDefecto()
        Toast.makeText(this, "Configuración restablecida a valores por defecto", Toast.LENGTH_SHORT).show()
    }
}
