package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SolicitarServicioActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private val REQUEST_CODE_GALLERY = 100
    private val REQUEST_CODE_CAMERA = 101
    private val REQUEST_PERMISSION = 200

    private val selectedImages = mutableListOf<Uri>()
    private var currentPhotoPath: String? = null

    private lateinit var etNombre: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etEquipo: EditText
    private lateinit var etProblema: EditText
    private lateinit var btnAgregarFotos: Button
    private lateinit var btnTomarFoto: Button
    private lateinit var btnEnviar: Button
    private lateinit var contenedorImagenes: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitar_servicio)

        db = FirebaseFirestore.getInstance()

        etNombre = findViewById(R.id.etNombre)
        etTelefono = findViewById(R.id.etTelefono)
        etEquipo = findViewById(R.id.etEquipo)
        etProblema = findViewById(R.id.etProblema)
        btnAgregarFotos = findViewById(R.id.btnAgregarFotos)
        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        btnEnviar = findViewById(R.id.btnEnviar)
        contenedorImagenes = findViewById(R.id.contenedorImagenes)

        btnAgregarFotos.setOnClickListener {
            if (checkPermissions()) abrirGaleria()
        }

        btnTomarFoto.setOnClickListener {
            if (checkPermissions()) tomarFoto()
        }

        btnEnviar.setOnClickListener {
            enviarSolicitud()
        }
    }

    private fun checkPermissions(): Boolean {
        val permisosNecesarios = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permisosNecesarios.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permisosNecesarios.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permisosNecesarios.add(Manifest.permission.CAMERA)
        }

        return if (permisosNecesarios.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permisosNecesarios.toTypedArray(), REQUEST_PERMISSION)
            false
        } else {
            true
        }
    }

    private fun abrirGaleria() {
        if (selectedImages.size >= 1) {
            Toast.makeText(this, "Solo puedes seleccionar una foto", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    private fun tomarFoto() {
        if (selectedImages.size >= 1) {
            Toast.makeText(this, "Solo puedes tomar una foto", Toast.LENGTH_SHORT).show()
            return
        }

        val fotoFile = crearArchivoImagen()
        fotoFile?.let {
            val fotoUri = FileProvider.getUriForFile(this, "$packageName.provider", it)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, fotoUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            startActivityForResult(intent, REQUEST_CODE_CAMERA)
        }
    }

    private fun crearArchivoImagen(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", cacheDir)
            currentPhotoPath = file.absolutePath
            file
        } catch (e: Exception) {
            Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show()
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_GALLERY -> {
                    data?.data?.let { uri ->
                        selectedImages.clear()
                        contenedorImagenes.removeAllViews()
                        selectedImages.add(uri)
                        mostrarImagen(uri)
                    }
                }

                REQUEST_CODE_CAMERA -> {
                    currentPhotoPath?.let {
                        val file = File(it)
                        if (file.exists()) {
                            val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
                            selectedImages.clear()
                            contenedorImagenes.removeAllViews()
                            selectedImages.add(uri)
                            mostrarImagen(uri)
                        }
                    }
                }
            }
        }
    }

    private fun mostrarImagen(uri: Uri) {
        val frameLayout = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                500
            ).apply {
                setMargins(0, 16, 0, 16)
            }
        }

        val imageView = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setImageURI(uri)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val btnCerrar = ImageButton(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                80, // ancho botón (puedes ajustar)
                80  // alto botón (puedes ajustar)
            ).apply {
                gravity = android.view.Gravity.END or android.view.Gravity.TOP
                marginEnd = 8
                topMargin = 8
            }
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel) // ícono "X" por defecto de Android
            background = null
            setOnClickListener {
                // Quitar la imagen de la lista y la vista
                selectedImages.remove(uri)
                contenedorImagenes.removeView(frameLayout)
            }
        }

        frameLayout.addView(imageView)
        frameLayout.addView(btnCerrar)

        contenedorImagenes.addView(frameLayout)
    }

    private fun enviarSolicitud() {
        val nombre = etNombre.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val equipo = etEquipo.text.toString().trim()
        val problema = etProblema.text.toString().trim()

        if (nombre.isEmpty() || telefono.isEmpty() || equipo.isEmpty() || problema.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val ordenId = "ORD" + System.currentTimeMillis().toString().takeLast(6)

        val datos = hashMapOf(
            "ordenId" to ordenId,
            "nombre" to nombre,
            "telefono" to telefono,
            "equipo" to equipo,
            "problema" to problema,
            "estado" to "Pendiente"
        )

        db.collection("ordenes_reparacion").document(ordenId)
            .set(datos)
            .addOnSuccessListener {
                Toast.makeText(this, "Orden registrada: $ordenId", Toast.LENGTH_LONG).show()
                val mensaje = """
                    ¡Hola! Necesito un servicio técnico.

                    🆔 Orden: $ordenId
                    📌 Nombre: $nombre
                    📞 Teléfono: $telefono
                    💻 Equipo: $equipo
                    🛠️ Problema: $problema
                """.trimIndent()

                enviarWhatsAppConFotos(mensaje, selectedImages)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al registrar orden", Toast.LENGTH_SHORT).show()
            }
    }
    private fun enviarWhatsAppConFotos(mensaje: String, fotos: List<Uri>) {
        val numeroWhatsApp = "51975167294" // Número sin '+', formato internacional sin espacios
        try {
            val intent = if (fotos.isEmpty()) {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, mensaje)
                    putExtra("jid", "$numeroWhatsApp@s.whatsapp.net")  // Esto abre chat directo con la empresa
                    setPackage("com.whatsapp")
                }
            } else {
                Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, fotos[0])
                    putExtra(Intent.EXTRA_TEXT, mensaje)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra("jid", "$numeroWhatsApp@s.whatsapp.net")  // chat directo con la empresa
                    setPackage("com.whatsapp")
                }
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

}
