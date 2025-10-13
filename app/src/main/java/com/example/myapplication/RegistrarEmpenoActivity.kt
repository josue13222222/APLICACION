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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class RegistrarEmpenoActivity : AppCompatActivity() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private var foto1Uri: Uri? = null
    private var foto2Uri: Uri? = null
    private lateinit var imgFoto1: ImageView
    private lateinit var imgFoto2: ImageView
    private lateinit var btnSeleccionarFoto1: Button
    private lateinit var btnSeleccionarFoto2: Button

    private val PICK_IMAGE_1 = 1
    private val PICK_IMAGE_2 = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_empeno)

        val etProducto = findViewById<EditText>(R.id.etProducto)
        val spinnerEstado = findViewById<Spinner>(R.id.spinnerEstado)
        val etValor = findViewById<EditText>(R.id.etValor)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)

        imgFoto1 = findViewById(R.id.imgFoto1)
        imgFoto2 = findViewById(R.id.imgFoto2)
        btnSeleccionarFoto1 = findViewById(R.id.btnSeleccionarFoto1)
        btnSeleccionarFoto2 = findViewById(R.id.btnSeleccionarFoto2)

        val estados = arrayOf("Nuevo", "Usado", "Regular")
        spinnerEstado.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)

        btnSeleccionarFoto1.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_1)
        }

        btnSeleccionarFoto2.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_2)
        }

        btnRegistrar.setOnClickListener {
            val producto = etProducto.text.toString().trim()
            val estado = spinnerEstado.selectedItem.toString()
            val valorStr = etValor.text.toString().trim()

            if (producto.isEmpty() || valorStr.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (foto1Uri == null) {
                Toast.makeText(this, "Debes agregar al menos una foto del artículo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val valor = valorStr.toIntOrNull()
            if (valor == null || valor <= 0) {
                Toast.makeText(this, "Ingresa un valor válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val puntos = valor / 100
            val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val uid = user.uid

            registrarEmpenoConFotos(uid, producto, estado, valor, puntos, fecha)
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
            // Redimensionar para no exceder límites de Firestore
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

    private fun registrarEmpenoConFotos(uid: String, producto: String, estado: String, valor: Int, puntos: Int, fecha: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Registrando empeño...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        // Convertir fotos a Base64
        val foto1Base64 = uriToBase64(foto1Uri!!)
        val foto2Base64 = if (foto2Uri != null) uriToBase64(foto2Uri!!) else ""

        if (foto1Base64.isEmpty()) {
            progressDialog.dismiss()
            Toast.makeText(this, "Error procesando la foto 1", Toast.LENGTH_SHORT).show()
            return
        }

        val docRef = db.collection("empenos").document()

        val empeno = hashMapOf(
            "id" to docRef.id,
            "producto" to producto,
            "estado" to estado,
            "valor" to valor,
            "puntos" to puntos,
            "fecha" to fecha,
            "userId" to uid,
            "foto1Url" to foto1Base64,
            "foto2Url" to foto2Base64,
            "estadoAprobacion" to "pendiente",
            "fechaCreacion" to com.google.firebase.Timestamp.now()
        )

        docRef.set(empeno)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Empeño registrado. Espera la aprobación del administrador para recibir $puntos puntos", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error guardando empeño: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
