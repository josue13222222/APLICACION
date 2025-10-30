package com.example.myapplication

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AdminRegistrarEmpenoActivity : AppCompatActivity() {
    private val db = Firebase.firestore

    private var foto1Uri: Uri? = null
    private var foto2Uri: Uri? = null
    private lateinit var imgFoto1: ImageView
    private lateinit var imgFoto2: ImageView
    private lateinit var btnSeleccionarFoto1: Button
    private lateinit var btnSeleccionarFoto2: Button
    private lateinit var btnRegistrar: Button

    private val PICK_IMAGE_1 = 1
    private val PICK_IMAGE_2 = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_registrar_empeno)

        val etProducto = findViewById<EditText>(R.id.etProducto)
        val etCliente = findViewById<EditText>(R.id.etCliente)
        val etMontoEmpenado = findViewById<EditText>(R.id.etMontoEmpenado)
        val etPrecioMensual = findViewById<EditText>(R.id.etPrecioMensual)
        btnRegistrar = findViewById(R.id.btnRegistrar)

        imgFoto1 = findViewById(R.id.imgFoto1)
        imgFoto2 = findViewById(R.id.imgFoto2)
        btnSeleccionarFoto1 = findViewById(R.id.btnSeleccionarFoto1)
        btnSeleccionarFoto2 = findViewById(R.id.btnSeleccionarFoto2)

        etMontoEmpenado.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val monto = s.toString().toDoubleOrNull()
                if (monto != null && monto > 0) {
                    val precioMensual = monto * 0.20 // Calcular 20%
                    etPrecioMensual.setText(String.format("%.2f", precioMensual))
                } else {
                    etPrecioMensual.setText("")
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

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
            val cliente = etCliente.text.toString().trim()
            val montoStr = etMontoEmpenado.text.toString().trim()
            val precioMensualStr = etPrecioMensual.text.toString().trim()

            if (producto.isEmpty() || cliente.isEmpty() || montoStr.isEmpty() || precioMensualStr.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (foto1Uri == null || foto2Uri == null) {
                Toast.makeText(this, "Debes agregar las 2 fotos del producto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!cliente.matches(Regex("^(\\+51|0)?[0-9]{9}$"))) {
                Toast.makeText(this, "Ingresa un número de teléfono válido de Perú (+51 o 9 dígitos)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val monto = montoStr.toDoubleOrNull()
            val precioMensual = precioMensualStr.toDoubleOrNull()

            if (monto == null || monto <= 0 || precioMensual == null || precioMensual <= 0) {
                Toast.makeText(this, "Ingresa valores válidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnRegistrar.isEnabled = false
            registrarEmpenoAdmin(producto, cliente, monto, precioMensual)
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

    private fun registrarEmpenoAdmin(producto: String, cliente: String, monto: Double, precioMensual: Double) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Registrando empeño...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        Thread {
            try {
                val foto1Base64 = uriToBase64(foto1Uri!!)
                val foto2Base64 = uriToBase64(foto2Uri!!)

                if (foto1Base64.isEmpty() || foto2Base64.isEmpty()) {
                    runOnUiThread {
                        progressDialog.dismiss()
                        btnRegistrar.isEnabled = true
                        Toast.makeText(this, "Error procesando las fotos", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val docRef = db.collection("empenos").document()
                val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                val empeno = hashMapOf(
                    "id" to docRef.id,
                    "producto" to producto,
                    "cliente" to cliente,
                    "montoEmpenado" to monto,
                    "precioMensual" to precioMensual,
                    "fecha" to fecha,
                    "foto1Url" to foto1Base64,
                    "foto2Url" to foto2Base64,
                    "estado" to "activo",
                    "userId" to cliente, // Guardando el email del cliente como userId para búsqueda
                    "fechaCreacion" to com.google.firebase.Timestamp.now()
                )

                docRef.set(empeno)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        btnRegistrar.isEnabled = true
                        Toast.makeText(this, "Empeño registrado exitosamente", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        btnRegistrar.isEnabled = true
                        Toast.makeText(this, "Error guardando empeño: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                runOnUiThread {
                    progressDialog.dismiss()
                    btnRegistrar.isEnabled = true
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
