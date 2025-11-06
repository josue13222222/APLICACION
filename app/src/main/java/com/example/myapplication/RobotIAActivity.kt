package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class RobotIAActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etMensaje: EditText
    private lateinit var btnEnviar: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: ChatAdapter
    private val mensajes = mutableListOf<ChatMessage>()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ⚠️ IMPORTANTE: cambia esta IP por la IP de tu PC (usa ipconfig)
    private val OLLAMA_URL = "http://192.168.0.233:11434/api/generate"
    private val OLLAMA_MODEL = "qwen2.5:0.5b" // modelo ligero ya descargado

    // 🧠 Instrucciones del asistente
    private val SYSTEM_PROMPT = """
        Eres "TechBot", un asistente experto en tecnología, reparaciones y ventas de equipos electrónicos.
        
        ÁREAS DE ESPECIALIZACIÓN:
        - Reparación de laptops, PCs y móviles
        - Diagnóstico de hardware/software
        - Componentes de computadoras (RAM, SSD, procesadores, tarjetas madre, etc.)
        - Problemas de conectividad (WiFi, Bluetooth, puertos USB)
        - Mantenimiento preventivo
        - Recomendaciones de compra
        - Soluciones de software
        
        INSTRUCCIONES:
        - Responde solo sobre temas tecnológicos o reparaciones.
        - Sé amable, técnico y breve.
        - Si la pregunta no es de tecnología, responde: 
          "Lo siento, solo puedo ayudarte con temas de tecnología, reparaciones o ventas de equipos."
        - Usa emojis para hacer la conversación más agradable.
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_robot_ia)

        Log.d("RobotIA", "✅ Activity iniciada")

        // Inicialización de vistas
        recyclerView = findViewById(R.id.recyclerViewChat)
        etMensaje = findViewById(R.id.etMensaje)
        btnEnviar = findViewById(R.id.btnEnviar)
        progressBar = findViewById(R.id.progressBar)

        adapter = ChatAdapter(mensajes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Mensaje de bienvenida
        agregarMensaje("🤖 ¡Hola! Soy TechBot, tu asistente especializado en tecnología 💻 ¿En qué puedo ayudarte hoy?", false)

        // Botón enviar
        btnEnviar.setOnClickListener {
            val mensaje = etMensaje.text.toString().trim()
            if (mensaje.isNotEmpty()) {
                enviarMensaje(mensaje)
            }
        }

        // Botón volver
        findViewById<ImageButton>(R.id.btnVolver).setOnClickListener {
            finish()
        }
    }

    private fun enviarMensaje(mensaje: String) {
        agregarMensaje(mensaje, true)
        etMensaje.text.clear()
        progressBar.visibility = View.VISIBLE
        btnEnviar.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = consultarOllama(mensaje)
                withContext(Dispatchers.Main) {
                    agregarMensaje(respuesta, false)
                    progressBar.visibility = View.GONE
                    btnEnviar.isEnabled = true
                }
            } catch (e: Exception) {
                Log.e("RobotIA", "❌ Error al consultar Ollama", e)
                withContext(Dispatchers.Main) {
                    val errorMsg = when {
                        e.message?.contains("Failed to connect") == true ->
                            "No se puede conectar con el servidor. Verifica que tu PC y tu celular estén en la misma red WiFi."
                        e.message?.contains("timeout") == true ->
                            "Tiempo de espera agotado. Intenta de nuevo."
                        else ->
                            "Error: ${e.message}"
                    }

                    agregarMensaje("⚠️ $errorMsg", false)
                    Toast.makeText(this@RobotIAActivity, errorMsg, Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                    btnEnviar.isEnabled = true
                }
            }
        }
    }

    private suspend fun consultarOllama(pregunta: String): String {
        return withContext(Dispatchers.IO) {
            val json = JSONObject().apply {
                put("model", OLLAMA_MODEL)
                put("prompt", "$SYSTEM_PROMPT\n\nUsuario: $pregunta\n\nAsistente:")
                put("stream", false)
                put("temperature", 0.7)
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(OLLAMA_URL)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IOException("Error en la respuesta: ${response.code} - ${response.message}")
            }

            val responseBody = response.body?.string() ?: throw IOException("Respuesta vacía")
            val jsonResponse = JSONObject(responseBody)
            jsonResponse.getString("response").trim()
        }
    }

    private fun agregarMensaje(texto: String, esUsuario: Boolean) {
        mensajes.add(ChatMessage(texto, esUsuario))
        adapter.notifyItemInserted(mensajes.size - 1)
        recyclerView.scrollToPosition(mensajes.size - 1)
    }
}

// Modelo de mensaje
data class ChatMessage(
    val texto: String,
    val esUsuario: Boolean
)
