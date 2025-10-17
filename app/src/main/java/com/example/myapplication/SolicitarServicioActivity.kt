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
import java.text.SimpleDateFormat
import java.util.*

class SolicitarServicioActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var foto1Uri: Uri? = null
    private var foto2Uri: Uri? = null
    private lateinit var imgFoto1: ImageView
    private lateinit var imgFoto2: ImageView
    private lateinit var btnSeleccionarFoto1: Button
    private lateinit var btnSeleccionarFoto2: Button

    private val PICK_IMAGE_1 = 1
    private val PICK_IMAGE_2 = 2

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
        btnSeleccionarFoto1 = findViewById(R.id.btnSeleccionarFoto1)
        btnSeleccionarFoto2 = findViewById(R.id.btnSeleccionarFoto2)

        btnSeleccionarFoto1.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_1)
        }

        btnSeleccionarFoto2.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_2)
        }

        btnEnviar.setOnClickListener {
            enviarSolicitud()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                PICK_IMAGE_1 -> {
                    foto1Uri = data.data
                    imgFoto1.setImageURI(foto1Uri)
                    imgFoto1.visibility = ImageView.VISIBLE
                }
                PICK_IMAGE_2 -> {
                    foto2Uri = data.data
                    imgFoto2.setImageURI(foto2Uri)
                    imgFoto2.visibility = ImageView.VISIBLE
                }
            }
        }
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

        if (foto1Uri == null) {
            Toast.makeText(this, "Debes agregar al menos la foto 1 del equipo", Toast.LENGTH_SHORT).show()
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

        val foto1Base64 = uriToBase64(foto1Uri!!)
        val foto2Base64 = if (foto2Uri != null) uriToBase64(foto2Uri!!) else ""

        if (foto1Base64.isEmpty()) {
            progressDialog.dismiss()
            Toast.makeText(this, "Error procesando la foto 1", Toast.LENGTH_SHORT).show()
            return
        }

        val ordenId = "ORD" + System.currentTimeMillis().toString().takeLast(6)

        val imagenesUrls = mutableListOf(foto1Base64)
        if (foto2Base64.isNotEmpty()) {
            imagenesUrls.add(foto2Base64)
        }

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
                Toast.makeText(this, "Orden registrada exitosamente: $ordenId", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Error al registrar orden", Toast.LENGTH_SHORT).show()
            }
    }
}
