package com.example.myapplication

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CuponesActivity : AppCompatActivity() {

    private lateinit var tvPuntos: TextView
    private lateinit var tvHistorial: TextView
    private lateinit var recyclerHistorial: RecyclerView
    private lateinit var btnCanjearPuntos: Button
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var sharedPreferences: SharedPreferences
    private val historialList = mutableListOf<HistorialPunto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cupones)

        tvPuntos = findViewById(R.id.tvPuntos)
        tvHistorial = findViewById(R.id.tvHistorial)
        recyclerHistorial = findViewById(R.id.rvCupones)
        btnCanjearPuntos = findViewById(R.id.btnCanjearPuntos)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        recyclerHistorial.layoutManager = LinearLayoutManager(this)

        cargarPuntosUsuario()
        cargarHistorialPuntos()

        btnCanjearPuntos.setOnClickListener {
            mostrarDialogoCanjearPuntos()
        }
    }

    override fun onResume() {
        super.onResume()
        cargarPuntosUsuario()
    }

    private fun cargarPuntosUsuario() {
        val uid = auth.currentUser?.uid

        if (uid.isNullOrEmpty()) {
            Log.e("[v0] CuponesActivity", "UID no disponible")
            tvPuntos.text = "Puntos disponibles: 0 (S/. 0.00)"
            return
        }

        db.collection("usuarios").document(uid)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("[v0] CuponesActivity", "Error al cargar puntos: ${error.message}")
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val puntos = document.getLong("puntos")?.toInt() ?: 0
                    val valorSoles = puntos * 0.50
                    tvPuntos.text = "Puntos disponibles: $puntos (S/. ${"%.2f".format(valorSoles)})"
                    Log.d("[v0] CuponesActivity", "Puntos cargados: $puntos para UID: $uid")
                } else {
                    tvPuntos.text = "Puntos disponibles: 0 (S/. 0.00)"
                    Log.d("[v0] CuponesActivity", "Usuario no encontrado")
                }
            }
    }

    private fun cargarHistorialPuntos() {
        val uid = auth.currentUser?.uid

        if (uid.isNullOrEmpty()) {
            Log.e("[v0] CuponesActivity", "UID no disponible para historial")
            return
        }

        db.collection("usuarios").document(uid)
            .collection("historial")
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("[v0] CuponesActivity", "Error al cargar historial: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    historialList.clear()
                    for (doc in snapshots) {
                        val transaccion = doc.toObject(HistorialPunto::class.java)
                        historialList.add(transaccion)
                    }
                    actualizarRecyclerView()
                    Log.d("[v0] CuponesActivity", "Historial cargado: ${historialList.size} registros")
                }
            }
    }

    private fun actualizarRecyclerView() {
        recyclerHistorial.adapter = HistorialPuntosAdapter(historialList)
    }

    private fun mostrarDialogoCanjearPuntos() {
        val uid = auth.currentUser?.uid

        if (uid.isNullOrEmpty()) {
            Log.e("[v0] CuponesActivity", "UID no disponible para canjear")
            return
        }

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                val puntosTotales = document.getLong("puntos")?.toInt() ?: 0

                if (puntosTotales == 0) {
                    val builder = android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog)
                    val view = android.widget.LinearLayout(this).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        setPadding(40, 40, 40, 40)

                        addView(android.widget.TextView(this@CuponesActivity).apply {
                            text = "😢 Sin Puntos Disponibles"
                            textSize = 20f
                            setTextColor(android.graphics.Color.WHITE)
                            setTypeface(null, android.graphics.Typeface.BOLD)
                        })

                        addView(android.widget.TextView(this@CuponesActivity).apply {
                            text = "\nNo tienes puntos para canjear en este momento.\n\n¿Sabías que?\n✓ Ganas puntos por cada compra\n✓ Ganas puntos por empeños\n✓ Ganas puntos por reparaciones\n\nContinúa comprando para acumular puntos y obtener increíbles descuentos."
                            textSize = 14f
                            setTextColor(android.graphics.Color.LTGRAY)
                            setPadding(0, 16, 0, 16)
                        })
                    }
                    builder.setView(view)
                        .setPositiveButton("ENTENDIDO") { dialog, _ -> dialog.dismiss() }
                        .show()
                    return@addOnSuccessListener
                }

                val editText = android.widget.EditText(this).apply {
                    inputType = android.text.InputType.TYPE_CLASS_NUMBER
                    hint = "Ingresa cantidad de puntos"
                    setHintTextColor(android.graphics.Color.GRAY)
                    setTextColor(android.graphics.Color.WHITE)
                    textSize = 16f
                    setPadding(16, 16, 16, 16)
                }

                val containerView = android.widget.LinearLayout(this).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    setPadding(32, 24, 32, 24)
                    addView(android.widget.TextView(this@CuponesActivity).apply {
                        text = "Tienes $puntosTotales puntos disponibles"
                        textSize = 16f
                        setTextColor(android.graphics.Color.WHITE)
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setPadding(0, 0, 0, 12)
                    })
                    addView(android.widget.TextView(this@CuponesActivity).apply {
                        text = "📊 Cada punto = S/. 0.50 de descuento"
                        textSize = 13f
                        setTextColor(android.graphics.Color.LTGRAY)
                        setPadding(0, 0, 0, 16)
                    })
                    addView(editText)
                }

                android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog)
                    .setTitle("🎁 Canjear Puntos")
                    .setView(containerView)
                    .setPositiveButton("CANJEAR") { _, _ ->
                        val puntosIngresados = editText.text.toString().toIntOrNull() ?: 0

                        when {
                            puntosIngresados <= 0 -> {
                                mostrarDialogoError("❌ Cantidad Inválida", "Por favor ingresa una cantidad válida de puntos (mínimo 1)")
                            }
                            puntosIngresados > puntosTotales -> {
                                mostrarDialogoError("⚠️ Puntos Insuficientes", "No tienes suficientes puntos.\n\n📊 Disponibles: $puntosTotales puntos\n📋 Solicitados: $puntosIngresados puntos\n\nTe faltan ${puntosIngresados - puntosTotales} puntos")
                            }
                            else -> {
                                val descuentoSoles = puntosIngresados * 0.50
                                val confirmView = android.widget.LinearLayout(this).apply {
                                    orientation = android.widget.LinearLayout.VERTICAL
                                    setPadding(32, 24, 32, 24)

                                    addView(android.widget.TextView(this@CuponesActivity).apply {
                                        text = "📋 RESUMEN DEL CANJE"
                                        textSize = 16f
                                        setTextColor(android.graphics.Color.WHITE)
                                        setTypeface(null, android.graphics.Typeface.BOLD)
                                        gravity = android.view.Gravity.CENTER
                                        setPadding(0, 0, 0, 20)
                                    })

                                    addView(android.widget.LinearLayout(this@CuponesActivity).apply {
                                        orientation = android.widget.LinearLayout.VERTICAL
                                        setBackgroundColor(android.graphics.Color.parseColor("#1E1E1E"))
                                        setPadding(16, 12, 16, 12)

                                        addView(android.widget.TextView(this@CuponesActivity).apply {
                                            text = "💎 Puntos a canjear: $puntosIngresados pts"
                                            textSize = 14f
                                            setTextColor(android.graphics.Color.WHITE)
                                            setPadding(0, 4, 0, 4)
                                        })

                                        addView(android.widget.TextView(this@CuponesActivity).apply {
                                            text = "💰 Descuento obtenido: S/. ${"%.2f".format(descuentoSoles)}"
                                            textSize = 14f
                                            setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                                            setTypeface(null, android.graphics.Typeface.BOLD)
                                            setPadding(0, 4, 0, 4)
                                        })

                                        addView(android.widget.TextView(this@CuponesActivity).apply {
                                            text = "Puntos restantes: ${puntosTotales - puntosIngresados} pts"
                                            textSize = 12f
                                            setTextColor(android.graphics.Color.LTGRAY)
                                            setPadding(0, 8, 0, 0)
                                        })
                                    })

                                    addView(android.widget.TextView(this@CuponesActivity).apply {
                                        text = "✅ El descuento se aplicará automáticamente en tu próxima compra"
                                        textSize = 12f
                                        setTextColor(android.graphics.Color.LTGRAY)
                                        setPadding(0, 16, 0, 0)
                                    })
                                }

                                android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog)
                                    .setView(confirmView)
                                    .setPositiveButton("✅ CONFIRMAR CANJE") { _, _ ->
                                        realizarCanjeoPuntos(uid, puntosIngresados)
                                    }
                                    .setNegativeButton("CANCELAR") { dialog, _ -> dialog.dismiss() }
                                    .show()
                            }
                        }
                    }
                    .setNegativeButton("CANCELAR") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
    }

    private fun mostrarDialogoError(titulo: String, mensaje: String) {
        val view = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)

            addView(android.widget.TextView(this@CuponesActivity).apply {
                text = titulo
                textSize = 18f
                setTextColor(android.graphics.Color.WHITE)
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, 12)
            })

            addView(android.widget.TextView(this@CuponesActivity).apply {
                text = mensaje
                textSize = 14f
                setTextColor(android.graphics.Color.LTGRAY)
            })
        }

        android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog)
            .setView(view)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun realizarCanjeoPuntos(uid: String, puntosCanjear: Int) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                val puntoActuales = document.getLong("puntos")?.toInt() ?: 0
                val nuevasCantidad = puntoActuales - puntosCanjear
                val descuentoSoles = puntosCanjear * 0.50

                db.collection("usuarios").document(uid)
                    .update("puntos", nuevasCantidad)
                    .addOnSuccessListener {
                        val historial = hashMapOf(
                            "tipo" to "Canje",
                            "descripcion" to "Canje de $puntosCanjear puntos - S/. ${"%.2f".format(descuentoSoles)} de descuento",
                            "puntos" to -puntosCanjear,
                            "fecha" to System.currentTimeMillis()
                        )

                        db.collection("usuarios").document(uid)
                            .collection("historial")
                            .add(historial)
                            .addOnSuccessListener {
                                Log.d("[v0] CuponesActivity", "Canje exitoso: $puntosCanjear puntos")

                                guardarNotificacionCanje(uid, puntosCanjear, descuentoSoles)

                                cargarPuntosUsuario()
                                cargarHistorialPuntos()

                                val successView = android.widget.LinearLayout(this).apply {
                                    orientation = android.widget.LinearLayout.VERTICAL
                                    setPadding(32, 24, 32, 24)
                                    gravity = android.view.Gravity.CENTER

                                    addView(android.widget.TextView(this@CuponesActivity).apply {
                                        text = "🎉 ¡CANJE EXITOSO!"
                                        textSize = 22f
                                        setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                                        setTypeface(null, android.graphics.Typeface.BOLD)
                                        gravity = android.view.Gravity.CENTER
                                        setPadding(0, 0, 0, 20)
                                    })

                                    addView(android.widget.LinearLayout(this@CuponesActivity).apply {
                                        orientation = android.widget.LinearLayout.VERTICAL
                                        setBackgroundColor(android.graphics.Color.parseColor("#1E1E1E"))
                                        setPadding(16, 16, 16, 16)

                                        addView(android.widget.TextView(this@CuponesActivity).apply {
                                            text = "✅ Canjeaste: $puntosCanjear puntos"
                                            textSize = 14f
                                            setTextColor(android.graphics.Color.WHITE)
                                            setPadding(0, 4, 0, 4)
                                        })

                                        addView(android.widget.TextView(this@CuponesActivity).apply {
                                            text = "💰 Descuento obtenido: S/. ${"%.2f".format(descuentoSoles)}"
                                            textSize = 14f
                                            setTextColor(android.graphics.Color.parseColor("#FFD700"))
                                            setTypeface(null, android.graphics.Typeface.BOLD)
                                            setPadding(0, 8, 0, 4)
                                        })

                                        addView(android.widget.TextView(this@CuponesActivity).apply {
                                            text = "📊 Puntos restantes: $nuevasCantidad"
                                            textSize = 12f
                                            setTextColor(android.graphics.Color.LTGRAY)
                                            setPadding(0, 8, 0, 0)
                                        })
                                    })

                                    addView(android.widget.TextView(this@CuponesActivity).apply {
                                        text = "\n🎁 Tu descuento ha sido guardado\n✨ Se aplicará automáticamente en tu próxima compra\n📲 Revisa tus notificaciones para más detalles"
                                        textSize = 12f
                                        setTextColor(android.graphics.Color.LTGRAY)
                                        gravity = android.view.Gravity.CENTER
                                        setPadding(0, 16, 0, 0)
                                    })
                                }

                                android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog)
                                    .setView(successView)
                                    .setPositiveButton("PERFECTO") { dialog, _ -> dialog.dismiss() }
                                    .setCancelable(false)
                                    .show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("[v0] CuponesActivity", "Error al registrar canje: ${e.message}")
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("[v0] CuponesActivity", "Error al actualizar puntos: ${e.message}")
                    }
            }
    }

    // New function to save redemption notification
    private fun guardarNotificacionCanje(uid: String, puntosCanjear: Int, descuentoSoles: Double) {
        val notificacion = hashMapOf(
            "userId" to uid,
            "titulo" to "¡Canje de Puntos Exitoso!",
            "descripcion" to "¡Felicidades! Canjeaste $puntosCanjear puntos y ganaste S/. ${"%.2f".format(descuentoSoles)} de descuento",
            "tipo" to "canje",
            "fecha" to java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date()),
            "fechaCreacion" to com.google.firebase.Timestamp.now(),
            "leida" to false
        )

        db.collection("notificaciones")
            .add(notificacion)
            .addOnSuccessListener {
                Log.d("[v0] CuponesActivity", "Notificación de canje guardada")
            }
            .addOnFailureListener { e ->
                Log.e("[v0] CuponesActivity", "Error al guardar notificación: ${e.message}")
            }
    }
}

data class HistorialPunto(
    val tipo: String = "",
    val descripcion: String = "",
    val puntos: Int = 0,
    val fecha: Long = 0
)
