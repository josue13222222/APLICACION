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
    private lateinit var etMonto: EditText
    private lateinit var cardYape: CardView
    private lateinit var cardPlin: CardView
    private lateinit var btnPagar: Button
    private lateinit var ivQR: ImageView

    private var metodoPagoSeleccionado = ""
    private var montoActual = 0.0
    private var usuarioActual = ""

    private val numeroContacto = "975167294"
    private val cuentaBCP = "36592395059088"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagos)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        usuarioActual = auth.currentUser?.uid ?: ""

        initViews()
        setupMetodosPago()
        setupPagarButton()
    }

    private fun initViews() {
        etMonto = findViewById(R.id.etMonto)
        cardYape = findViewById(R.id.cardYape)
        cardPlin = findViewById(R.id.cardPlin)
        btnPagar = findViewById(R.id.btnPagar)
        ivQR = findViewById(R.id.ivQR)
    }

    private fun setupMetodosPago() {
        cardYape.setOnClickListener {
            seleccionarMetodoPago("Yape", cardYape)
        }

        cardPlin.setOnClickListener {
            seleccionarMetodoPago("Plin", cardPlin)
        }
    }

    private fun seleccionarMetodoPago(metodo: String, card: CardView) {
        cardYape.setCardBackgroundColor(getColor(R.color.card_background))
        cardPlin.setCardBackgroundColor(getColor(R.color.card_background))

        card.setCardBackgroundColor(getColor(R.color.primary_color))
        metodoPagoSeleccionado = metodo
    }

    private fun setupPagarButton() {
        btnPagar.setOnClickListener {
            if (validarDatos()) {
                montoActual = etMonto.text.toString().toDoubleOrNull() ?: 0.0

                mostrarDialogoQRyPago()
            }
        }
    }

    private fun validarDatos(): Boolean {
        if (metodoPagoSeleccionado.isEmpty()) {
            Toast.makeText(this, "Selecciona un método de pago (Yape o Plin)", Toast.LENGTH_SHORT).show()
            return false
        }

        if (etMonto.text.toString().isEmpty()) {
            Toast.makeText(this, "Ingresa el monto", Toast.LENGTH_SHORT).show()
            return false
        }

        val monto = etMonto.text.toString().toDoubleOrNull() ?: 0.0
        if (monto <= 0) {
            Toast.makeText(this, "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun mostrarDialogoQRyPago() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Pasos para Realizar Pago")
        builder.setMessage(
            """
            ✅ Abre tu aplicación $metodoPagoSeleccionado
            
            📱 Monto a pagar: S/. ${"%.2f".format(montoActual)}
            
            🔗 Escanea el QR mostrado arriba o transfiere a:
            
            📞 WhatsApp: $numeroContacto
            🏦 Cuenta BCP: $cuentaBCP
            
            ¿Continuar?
            """.trimIndent()
        )
        builder.setPositiveButton("Continuar") { _, _ ->
            abrirAplicacionPago(metodoPagoSeleccionado)
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun abrirAplicacionPago(app: String) {
        val monto = etMonto.text.toString()

        val intent = when (app) {
            "Yape" -> {
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("yape://")
                }
            }
            "Plin" -> {
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("plin://")
                }
            }
            else -> null
        }

        if (intent != null && intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                mostrarDialogoConfirmacionPago()
            }, 2000)
        } else {
            Toast.makeText(this, "$app no está instalada. Por favor instálala y reinenta.", Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarDialogoConfirmacionPago() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Confirmación de Pago")
        builder.setMessage("¿Ya completaste el pago en $metodoPagoSeleccionado por S/. ${"%.2f".format(montoActual)}?")
        builder.setPositiveButton("Sí, ya pagué") { _, _ ->
            procesarPago()
        }
        builder.setNegativeButton("Todavía no", null)
        builder.show()
    }

    private fun procesarPago() {
        val userId = auth.currentUser?.uid ?: return
        val monto = etMonto.text.toString().toDoubleOrNull() ?: 0.0

        val pago = Pago(
            id = firestore.collection("pagos").document().id,
            userId = userId,
            servicioTipo = "Pago General",
            monto = monto,
            metodoPago = metodoPagoSeleccionado,
            numeroReferencia = "AUTO-${System.currentTimeMillis()}",
            descripcion = "Pago realizado en $metodoPagoSeleccionado",
            estado = "Confirmado",
            fecha = Timestamp.now(),
            numeroContacto = numeroContacto
        )

        firestore.collection("pagos").document(pago.id).set(pago)
            .addOnSuccessListener {
                agregarPuntosAlUsuario(monto)

                Toast.makeText(this, "✅ Pago registrado exitosamente", Toast.LENGTH_LONG).show()
                enviarWhatsApp(pago, monto)

                etMonto.text.clear()
                metodoPagoSeleccionado = ""
                cardYape.setCardBackgroundColor(getColor(R.color.card_background))
                cardPlin.setCardBackgroundColor(getColor(R.color.card_background))

                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    finish()
                }, 1500)
            }
            .addOnFailureListener {
                Toast.makeText(this, "❌ Error al registrar el pago", Toast.LENGTH_SHORT).show()
            }
    }

    private fun agregarPuntosAlUsuario(monto: Double) {
        val puntosGanados = (monto / 100).toInt()

        if (puntosGanados > 0) {
            firestore.collection("usuarios").document(usuarioActual).update(
                "puntos", com.google.firebase.firestore.FieldValue.increment(puntosGanados.toLong())
            )
                .addOnSuccessListener {
                    Toast.makeText(this, "🎉 +$puntosGanados puntos acumulados!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun enviarWhatsApp(pago: Pago, monto: Double) {
        val mensaje = """
            🤖 *ROBOT AL RESCATE - PAGO CONFIRMADO*
            
            💰 *Monto:* S/. ${"%.2f".format(monto)}
            💳 *Método:* ${pago.metodoPago}
            ⏰ *Fecha:* ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(pago.fecha.toDate())}
            
            ✅ Pago registrado en el sistema
            🎁 Puntos acumulados: ${(monto / 100).toInt()}
            
            ¡Gracias por tu compra!
        """.trimIndent()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/51$numeroContacto?text=${Uri.encode(mensaje)}")
        }
        startActivity(intent)
    }
}
