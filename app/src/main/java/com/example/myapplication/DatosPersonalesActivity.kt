package com.example.myapplication

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class DatosPersonalesActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etApellidoPaterno: EditText
    private lateinit var etApellidoMaterno: EditText
    private lateinit var etDni: EditText
    private lateinit var etCelular: EditText
    private lateinit var etFechaNacimiento: EditText
    private lateinit var btnGuardar: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_personales)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etNombre = findViewById(R.id.etNombre)
        etApellidoPaterno = findViewById(R.id.etApellidoPaterno)
        etApellidoMaterno = findViewById(R.id.etApellidoMaterno)
        etDni = findViewById(R.id.etDni)
        etCelular = findViewById(R.id.etCelular)
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento)
        btnGuardar = findViewById(R.id.btnGuardar)

        etFechaNacimiento.setOnClickListener { mostrarDatePicker() }
        btnGuardar.setOnClickListener { guardarDatos() }

        cargarDatos() // Cargar datos si existen en Firestore
    }

    private fun mostrarDatePicker() {
        val calendario = Calendar.getInstance()
        val anio = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val fecha = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            etFechaNacimiento.setText(fecha)
        }, anio, mes, dia)

        datePicker.show()
    }

    private fun guardarDatos() {
        val nombre = etNombre.text.toString().trim()
        val apellidoPaterno = etApellidoPaterno.text.toString().trim()
        val apellidoMaterno = etApellidoMaterno.text.toString().trim()
        val dni = etDni.text.toString().trim()
        val celular = etCelular.text.toString().trim()
        val fechaNacimiento = etFechaNacimiento.text.toString().trim()

        if (nombre.isEmpty() || apellidoPaterno.isEmpty() || dni.isEmpty() || celular.isEmpty() || fechaNacimiento.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val datosUsuario = hashMapOf(
            "nombre" to nombre,
            "apellidoPaterno" to apellidoPaterno,
            "apellidoMaterno" to apellidoMaterno,
            "dni" to dni,
            "celular" to celular,
            "fechaNacimiento" to fechaNacimiento
        )

        db.collection("usuarios").document(userId)
            .set(datosUsuario) // Usa set() para evitar errores si el documento no existe
            .addOnSuccessListener {
                Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar los datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarDatos() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    etNombre.setText(document.getString("nombre") ?: "")
                    etApellidoPaterno.setText(document.getString("apellidoPaterno") ?: "")
                    etApellidoMaterno.setText(document.getString("apellidoMaterno") ?: "")
                    etDni.setText(document.getString("dni") ?: "")
                    etCelular.setText(document.getString("celular") ?: "")
                    etFechaNacimiento.setText(document.getString("fechaNacimiento") ?: "")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar los datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
