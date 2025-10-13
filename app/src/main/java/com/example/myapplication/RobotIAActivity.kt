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

    private val OLLAMA_URL = "http://10.0.2.2:11434/api/generate"

    private val SYSTEM_PROMPT = """
        Eres "Robot IA", un asistente especializado en reparaciones de tecnología, específicamente en:
        - Reparación de laptops y computadoras
        - Diagnóstico de problemas de hardware
        - Componentes de computadoras (RAM, disco duro, procesador, tarjeta madre, etc.)
        - Problemas de software relacionados con reparaciones
        - Mantenimiento preventivo de equipos
        - Reemplazo de piezas y componentes
        
        IMPORTANTE:
        - Solo respondes preguntas relacionadas con reparaciones de tecnología
        - Si te preguntan sobre otros temas, responde: "Lo siento, solo estoy entrenado para ayudarte con consultas sobre reparaciones de laptops y componentes de tecnología. ¿Tienes alguna pregunta sobre reparaciones?"
        - Sé breve, claro y técnico en tus respuestas
        - Proporciona soluciones prácticas
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_robot_ia)

        Log.d("RobotIA", "[v0] Activity iniciada")

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerViewChat)
        etMensaje = findViewById(R.id.etMensaje)
        btnEnviar = findViewById(R.id.btnEnviar)
        progressBar = findViewById(R.id.progressBar)

        // Configurar RecyclerView
        adapter = ChatAdapter(mensajes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Mensaje de bienvenida
        agregarMensaje("¡Hola! Soy Robot IA, tu asistente especializado en reparaciones de tecnología. ¿En qué puedo ayudarte hoy?", false)

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
        Log.d("RobotIA", "[v0] Enviando mensaje: $mensaje")

        // Agregar mensaje del usuario
        agregarMensaje(mensaje, true)
        etMensaje.text.clear()

        // Mostrar loading
        progressBar.visibility = View.VISIBLE
        btnEnviar.isEnabled = false

        // Enviar a Ollama
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("RobotIA", "[v0] Consultando Ollama...")
                val respuesta = consultarOllama(mensaje)
                Log.d("RobotIA", "[v0] Respuesta recibida: $respuesta")

                withContext(Dispatchers.Main) {
                    agregarMensaje(respuesta, false)
                    progressBar.visibility = View.GONE
                    btnEnviar.isEnabled = true
                }
            } catch (e: Exception) {
                Log.e("RobotIA", "[v0] Error al consultar Ollama", e)
                withContext(Dispatchers.Main) {
                    val errorMsg = when {
                        e.message?.contains("Failed to connect") == true ->
                            "No se puede conectar con Ollama. Verifica que esté corriendo en tu PC con 'ollama serve'"
                        e.message?.contains("timeout") == true ->
                            "Tiempo de espera agotado. Ollama está tardando mucho en responder."
                        else ->
                            "Error: ${e.message}"
                    }

                    agregarMensaje("❌ $errorMsg", false)
                    Toast.makeText(this@RobotIAActivity, errorMsg, Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                    btnEnviar.isEnabled = true
                }
            }
        }
    }

    private suspend fun consultarOllama(pregunta: String): String {
        return withContext(Dispatchers.IO) {
            Log.d("RobotIA", "[v0] URL: $OLLAMA_URL")

            val json = JSONObject().apply {
                put("model", "qwen2.5:1.5b") // Cambia al modelo que descargaste
                put("prompt", "$SYSTEM_PROMPT\n\nUsuario: $pregunta\nRobot IA:")
                put("stream", false)
            }

            Log.d("RobotIA", "[v0] JSON Request: ${json.toString()}")

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(OLLAMA_URL)
                .post(body)
                .build()

            Log.d("RobotIA", "[v0] Enviando petición HTTP...")
            val response = client.newCall(request).execute()
            Log.d("RobotIA", "[v0] Código de respuesta: ${response.code}")

            if (!response.isSuccessful) {
                throw IOException("Error en la respuesta: ${response.code} - ${response.message}")
            }

            val responseBody = response.body?.string() ?: throw IOException("Respuesta vacía")
            Log.d("RobotIA", "[v0] Response Body: $responseBody")

            val jsonResponse = JSONObject(responseBody)
            val respuestaTexto = jsonResponse.getString("response")

            if (respuestaTexto.isBlank()) {
                throw IOException("Ollama devolvió una respuesta vacía")
            }

            respuestaTexto
        }
    }

    private fun agregarMensaje(texto: String, esUsuario: Boolean) {
        mensajes.add(ChatMessage(texto, esUsuario))
        adapter.notifyItemInserted(mensajes.size - 1)
        recyclerView.scrollToPosition(mensajes.size - 1)
    }
}

// Modelo de datos para mensajes
data class ChatMessage(
    val texto: String,
    val esUsuario: Boolean
)
