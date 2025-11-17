package com.example.myapplication

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class SolicitarServicioActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var foto1Uri: Uri? = null
    private var foto2Uri: Uri? = null
    private var foto3Uri: Uri? = null
    private var foto4Uri: Uri? = null
    private lateinit var imgFoto1: ImageView
    private lateinit var imgFoto2: ImageView
    private lateinit var imgFoto3: ImageView
    private lateinit var imgFoto4: ImageView
    private lateinit var btnSeleccionarFoto1: Button
    private lateinit var btnSeleccionarFoto2: Button
    private lateinit var btnSeleccionarFoto3: Button
    private lateinit var btnSeleccionarFoto4: Button

    private val PICK_IMAGE_1 = 1
    private val PICK_IMAGE_2 = 2
    private val PICK_IMAGE_3 = 3
    private val PICK_IMAGE_4 = 4
    private val CAMERA_REQUEST_1 = 101
    private val CAMERA_REQUEST_2 = 102
    private val CAMERA_REQUEST_3 = 103
    private val CAMERA_REQUEST_4 = 104

    private lateinit var etNombre: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etEquipo: EditText
    private lateinit var etProblema: EditText
    private lateinit var btnEnviar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitar_servicio)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        etNombre = findViewById(R.id.etNombre)
        etTelefono = findViewById(R.id.etTelefono)
        etEquipo = findViewById(R.id.etEquipo)
        etProblema = findViewById(R.id.etProblema)
        btnEnviar = findViewById(R.id.btnEnviar)

        imgFoto1 = findViewById(R.id.imgFoto1)
        imgFoto2 = findViewById(R.id.imgFoto2)
        imgFoto3 = findViewById(R.id.imgFoto3)
        imgFoto4 = findViewById(R.id.imgFoto4)
        btnSeleccionarFoto1 = findViewById(R.id.btnSeleccionarFoto1)
        btnSeleccionarFoto2 = findViewById(R.id.btnSeleccionarFoto2)
        btnSeleccionarFoto3 = findViewById(R.id.btnSeleccionarFoto3)
        btnSeleccionarFoto4 = findViewById(R.id.btnSeleccionarFoto4)

        btnSeleccionarFoto1.setOnClickListener { mostrarOpcionesFoto(1) }
        btnSeleccionarFoto2.setOnClickListener { mostrarOpcionesFoto(2) }
        btnSeleccionarFoto3.setOnClickListener { mostrarOpcionesFoto(3) }
        btnSeleccionarFoto4.setOnClickListener { mostrarOpcionesFoto(4) }

        btnEnviar.setOnClickListener {
            enviarSolicitud()
        }
    }

    private fun mostrarOpcionesFoto(numeroFoto: Int) {
        val opciones = arrayOf("Tomar foto con cámara", "Seleccionar de galería")
        android.app.AlertDialog.Builder(this)
            .setTitle("Agregar Foto $numeroFoto")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> abrirCamara(numeroFoto)
                    1 -> abrirGaleria(numeroFoto)
                }
            }
            .show()
    }

    private fun abrirCamara(numeroFoto: Int) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val cameraRequestCode = when (numeroFoto) {
            1 -> CAMERA_REQUEST_1
            2 -> CAMERA_REQUEST_2
            3 -> CAMERA_REQUEST_3
            else -> CAMERA_REQUEST_4
        }
        startActivityForResult(intent, cameraRequestCode)
    }

    private fun abrirGaleria(numeroFoto: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val pickImageCode = when (numeroFoto) {
            1 -> PICK_IMAGE_1
            2 -> PICK_IMAGE_2
            3 -> PICK_IMAGE_3
            else -> PICK_IMAGE_4
        }
        startActivityForResult(intent, pickImageCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_1 -> {
                    foto1Uri = data?.data
                    imgFoto1.setImageURI(foto1Uri)
                    imgFoto1.visibility = ImageView.VISIBLE
                }
                PICK_IMAGE_2 -> {
                    foto2Uri = data?.data
                    imgFoto2.setImageURI(foto2Uri)
                    imgFoto2.visibility = ImageView.VISIBLE
                }
                PICK_IMAGE_3 -> {
                    foto3Uri = data?.data
                    imgFoto3.setImageURI(foto3Uri)
                    imgFoto3.visibility = ImageView.VISIBLE
                }
                PICK_IMAGE_4 -> {
                    foto4Uri = data?.data
                    imgFoto4.setImageURI(foto4Uri)
                    imgFoto4.visibility = ImageView.VISIBLE
                }
                CAMERA_REQUEST_1 -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        imgFoto1.setImageBitmap(it)
                        imgFoto1.visibility = ImageView.VISIBLE
                        foto1Uri = guardarBitmapEnUri(it)
                    }
                }
                CAMERA_REQUEST_2 -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        imgFoto2.setImageBitmap(it)
                        imgFoto2.visibility = ImageView.VISIBLE
                        foto2Uri = guardarBitmapEnUri(it)
                    }
                }
                CAMERA_REQUEST_3 -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        imgFoto3.setImageBitmap(it)
                        imgFoto3.visibility = ImageView.VISIBLE
                        foto3Uri = guardarBitmapEnUri(it)
                    }
                }
                CAMERA_REQUEST_4 -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        imgFoto4.setImageBitmap(it)
                        imgFoto4.visibility = ImageView.VISIBLE
                        foto4Uri = guardarBitmapEnUri(it)
                    }
                }
            }
        }
    }

    private fun guardarBitmapEnUri(bitmap: Bitmap): Uri {
        val file = File(cacheDir, "foto_${System.currentTimeMillis()}.jpg")
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        fos.close()
        return Uri.fromFile(file)
    }

    private fun uriToBase64(uri: Uri): String {
        return try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true)
            val byteArrayOutputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
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

        if (foto1Uri == null || foto2Uri == null || foto3Uri == null || foto4Uri == null) {
            Toast.makeText(this, "❌ DEBES AGREGAR LAS 4 FOTOS OBLIGATORIAS", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión para registrar una orden", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Registrando orden...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val imagenesUrls = mutableListOf<String>()
        if (foto1Uri != null) imagenesUrls.add(uriToBase64(foto1Uri!!))
        if (foto2Uri != null) imagenesUrls.add(uriToBase64(foto2Uri!!))
        if (foto3Uri != null) imagenesUrls.add(uriToBase64(foto3Uri!!))
        if (foto4Uri != null) imagenesUrls.add(uriToBase64(foto4Uri!!))

        if (imagenesUrls.isEmpty()) {
            progressDialog.dismiss()
            Toast.makeText(this, "Error procesando las fotos", Toast.LENGTH_SHORT).show()
            return
        }

        val ordenId = "ORD" + System.currentTimeMillis().toString().takeLast(6)

        val datos = hashMapOf(
            "ordenId" to ordenId,
            "nombre" to nombre,
            "telefono" to telefono,
            "equipo" to equipo,
            "problema" to problema,
            "estado" to "Pendiente",
            "timestamp" to System.currentTimeMillis(),
            "imagenes" to imagenesUrls,
            "userId" to user.uid
        )

        db.collection("ordenes_reparacion").document(ordenId)
            .set(datos)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "✅ Orden registrada exitosamente: $ordenId", Toast.LENGTH_LONG).show()
                println("[v0] Orden guardada en Firebase: $ordenId")
                finish()
            }
            .addOnFailureListener { error ->
                progressDialog.dismiss()
                println("[v0] Error al guardar orden: ${error.message}")
                Toast.makeText(this, "Error al registrar orden: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
