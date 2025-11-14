package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import android.graphics.Bitmap
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
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
        ivQR = findViewById(R.id.ivQRDialog)
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

                mostrarDialogoQRConMonto()
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

    private fun mostrarDialogoQRConMonto() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_qr_pago, null)
        val ivQRDialog = view.findViewById<ImageView>(R.id.ivQRDialog)
        val tvMonto = view.findViewById<TextView>(R.id.tvMontoQR)
        val ivLogo = view.findViewById<ImageView>(R.id.ivLogoPago)
        val btnCapturar = view.findViewById<Button>(R.id.btnCapturar)
        val btnCancelar = view.findViewById<Button>(R.id.btnCancelar)

        val qrBitmap = GeneradorQR.generarQRPago(montoActual, numeroContacto)
        if (qrBitmap == null) {
            Toast.makeText(this, "Error al generar QR", Toast.LENGTH_SHORT).show()
            return
        }

        ivQRDialog.setImageBitmap(qrBitmap)

        tvMonto.text = "S/. ${"%.2f".format(montoActual)}"

        val logoResId = if (metodoPagoSeleccionado == "Yape") {
            R.drawable.yape_logo
        } else {
            R.drawable.plin_logo
        }
        ivLogo.setImageResource(logoResId)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        btnCapturar.setOnClickListener {
            guardarPagoPendiente(view, qrBitmap)
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun guardarPagoPendiente(view: android.view.View, qrBitmap: Bitmap) {
        val userId = auth.currentUser?.uid ?: return
        val correoUsuario = auth.currentUser?.email ?: ""
        val monto = etMonto.text.toString().toDoubleOrNull() ?: 0.0

        val capturaFile = capturarYGuardarPantalla(view, qrBitmap)

        val pago = Pago(
            id = firestore.collection("pagos").document().id,
            userId = userId,
            correoUsuario = correoUsuario,
            servicioTipo = "Pago General",
            monto = monto,
            metodoPago = metodoPagoSeleccionado,
            numeroReferencia = "AUTO-${System.currentTimeMillis()}",
            descripcion = "Pago realizado en $metodoPagoSeleccionado",
            estado = "Pendiente",
            fecha = Timestamp.now(),
            numeroContacto = numeroContacto
        )

        firestore.collection("pagos").document(pago.id).set(pago)
            .addOnSuccessListener {
                Toast.makeText(this, "Pago guardado como pendiente", Toast.LENGTH_SHORT).show()

                abrirAplicacionPago(metodoPagoSeleccionado)

                etMonto.text.clear()
                metodoPagoSeleccionado = ""
                cardYape.setCardBackgroundColor(getColor(R.color.card_background))
                cardPlin.setCardBackgroundColor(getColor(R.color.card_background))
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar el pago", Toast.LENGTH_SHORT).show()
            }
    }

    private fun capturarYGuardarPantalla(view: android.view.View, qrBitmap: Bitmap): java.io.File {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        view.draw(canvas)

        val file = java.io.File(getExternalFilesDir(null), "pago_qr_${System.currentTimeMillis()}.png")
        val fos = java.io.FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()

        return file
    }

    private fun abrirAplicacionPago(app: String) {
        val intent = when (app) {
            "Yape" -> {
                val yapeIntent = Intent()
                yapeIntent.setPackage("com.yape.android")
                yapeIntent.setAction(Intent.ACTION_MAIN)
                yapeIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                yapeIntent
            }
            "Plin" -> {
                val plinIntent = Intent()
                plinIntent.setPackage("com.primax.plin")
                plinIntent.setAction(Intent.ACTION_MAIN)
                plinIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                plinIntent
            }
            else -> null
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Si la app no está instalada, abre la web como alternativa
            val webIntent = when (app) {
                "Yape" -> Intent(Intent.ACTION_VIEW, Uri.parse("https://www.yape.com.pe"))
                "Plin" -> Intent(Intent.ACTION_VIEW, Uri.parse("https://www.plin.pe"))
                else -> null
            }
            startActivity(webIntent)
        }
    }

    private fun procesarPago() {
        val userId = auth.currentUser?.uid ?: return
        val monto = etMonto.text.toString().toDoubleOrNull() ?: 0.0

        val pago = Pago(
            id = firestore.collection("pagos").document().id,
            userId = userId,
            correoUsuario = "",
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
                    mostrarDialogoPuntosGanados(puntosGanados)
                }
        }
    }

    private fun mostrarDialogoPuntosGanados(puntos: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("🎉 ¡Puntos Acumulados!")
        builder.setMessage("Has ganado $puntos puntos con esta compra.\n\nTotal puntos en tu cuenta: $puntos")
        builder.setPositiveButton("Ver mis puntos") { _, _ ->
            startActivity(Intent(this, MisPuntosActivity::class.java))
        }
        builder.setNegativeButton("Cerrar", null)
        builder.show()
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
