package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAddressFormBinding

class AddressFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddressFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding
        binding = ActivityAddressFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Listener del botón "Guardar"
        binding.btnGuardar.setOnClickListener {
            if (validarCampos()) {
                guardarDireccion()
            }
        }
    }

    private fun validarCampos(): Boolean {
        val campos = listOf(
            binding.etNombre to "Nombre",
            binding.etCalle to "Calle",
            binding.etCiudad to "Ciudad",
            binding.etCodigoPostal to "Código postal",
            binding.etTelefono to "Teléfono"
        )

        for ((campo, nombre) in campos) {
            if (campo.text.isNullOrEmpty()) {
                campo.error = "Este campo es obligatorio"
                return false
            }
        }
        return true
    }

    private fun guardarDireccion() {
        val direccion = mapOf(
            "nombre" to binding.etNombre.text.toString(),
            "calle" to binding.etCalle.text.toString(),
            "ciudad" to binding.etCiudad.text.toString(),
            "codigoPostal" to binding.etCodigoPostal.text.toString(),
            "telefono" to binding.etTelefono.text.toString(),
            "notas" to binding.etNotas.text.toString()
        )

        // Aquí puedes guardar en SharedPreferences, Room, o enviar a un servidor
        Toast.makeText(this, "Dirección guardada: $direccion", Toast.LENGTH_LONG).show()
        finish() // Cierra la actividad después de guardar
    }
}
