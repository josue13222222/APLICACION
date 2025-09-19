package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class PagosActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var spinnerServicio: Spinner
    private lateinit var etMonto: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etReferencia: EditText
    private lateinit var cardYape: CardView
    private lateinit var cardPlin: CardView
    private lateinit var cardBCP: CardView
    private lateinit var btnPagar: Button
    private lateinit var tvNumeroContacto: TextView
    private lateinit var tvCuentaBCP: TextView

    private var metodoPagoSeleccionado = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagos)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initViews()
        setupSpinner()
        setupMetodosPago()
        setupPagarButton()
    }

    private fun initViews() {
        spinnerServicio = findViewById(R.id.spinnerServicio)
        etMonto = findViewById(R.id.etMonto)
        etDescripcion = findViewById(R.id.etDescripcion)
        etReferencia = findViewById(R.id.etReferencia)
        cardYape = findViewById(R.id.cardYape)
        cardPlin = findViewById(R.id.cardPlin)
        cardBCP = findViewById(R.id.cardBCP)
        btnPagar = findViewById(R.id.btnPagar)
        tvNumeroContacto = findViewById(R.id.tvNumeroContacto)
        tvCuentaBCP = findViewById(R.id.tvCuentaBCP)
    }

    private fun setupSpinner() {
        val servicios = arrayOf("Robot Rescate", "Robot Empeño")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, servicios)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerServicio.adapter = adapter
    }

    private fun setupMetodosPago() {
        cardYape.setOnClickListener {
            seleccionarMetodoPago("Yape", cardYape)
            mostrarInformacionContacto()
        }

        cardPlin.setOnClickListener {
            seleccionarMetodoPago("Plin", cardPlin)
            mostrarInformacionContacto()
        }

        cardBCP.setOnClickListener {
            seleccionarMetodoPago("BCP", cardBCP)
            mostrarInformacionBCP()
        }
    }

    private fun seleccionarMetodoPago(metodo: String, card: CardView) {
        // Resetear todas las tarjetas
        cardYape.setCardBackgroundColor(getColor(R.color.card_background))
        cardPlin.setCardBackgroundColor(getColor(R.color.card_background))
        cardBCP.setCardBackgroundColor(getColor(R.color.card_background))

        // Seleccionar la tarjeta actual
        card.setCardBackgroundColor(getColor(R.color.primary_color))
        metodoPagoSeleccionado = metodo
    }

    private fun mostrarInformacionContacto() {
        tvNumeroContacto.text = "Número: 93419837"
        tvCuentaBCP.text = ""
    }

    private fun mostrarInformacionBCP() {
        tvCuentaBCP.text = "Cuenta BCP: 191-123456789-0-12"
        tvNumeroContacto.text = ""
    }

    private fun setupPagarButton() {
        btnPagar.setOnClickListener {
            if (validarDatos()) {
                procesarPago()
            }
        }
    }

    private fun validarDatos(): Boolean {
        if (metodoPagoSeleccionado.isEmpty()) {
            Toast.makeText(this, "Selecciona un método de pago", Toast.LENGTH_SHORT).show()
            return false
        }

        if (etMonto.text.toString().isEmpty()) {
            Toast.makeText(this, "Ingresa el monto", Toast.LENGTH_SHORT).show()
            return false
        }

        if (etReferencia.text.toString().isEmpty()) {
            Toast.makeText(this, "Ingresa el número de referencia", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun procesarPago() {
        val userId = auth.currentUser?.uid ?: return
        val servicioSeleccionado = spinnerServicio.selectedItem.toString()
        val monto = etMonto.text.toString().toDoubleOrNull() ?: 0.0
        val descripcion = etDescripcion.text.toString()
        val referencia = etReferencia.text.toString()

        val pago = Pago(
            id = firestore.collection("pagos").document().id,
            userId = userId,
            servicioTipo = servicioSeleccionado,
            monto = monto,
            metodoPago = metodoPagoSeleccionado,
            numeroReferencia = referencia,
            descripcion = descripcion,
            fecha = Timestamp.now()
        )

        firestore.collection("pagos").document(pago.id).set(pago)
            .addOnSuccessListener {
                Toast.makeText(this, "Pago registrado exitosamente", Toast.LENGTH_LONG).show()

                // Abrir WhatsApp con información del pago
                enviarWhatsApp(pago)

                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al registrar el pago", Toast.LENGTH_SHORT).show()
            }
    }

    private fun enviarWhatsApp(pago: Pago) {
        val mensaje = """
            🤖 *ROBOT AL RESCATE - PAGO REGISTRADO*
            
            📋 *Servicio:* ${pago.servicioTipo}
            💰 *Monto:* S/ ${pago.monto}
            💳 *Método:* ${pago.metodoPago}
            🔢 *Referencia:* ${pago.numeroReferencia}
            📝 *Descripción:* ${pago.descripcion}
            
            ⏰ *Fecha:* ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(pago.fecha.toDate())}
            
            Por favor, confirma la recepción del pago.
        """.trimIndent()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/51${pago.numeroContacto}?text=${Uri.encode(mensaje)}")
        }
        startActivity(intent)
    }
}
