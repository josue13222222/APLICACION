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

    // 🧠 Instrucciones del asistente - Mejorado para especialización en reparaciones
    private val SYSTEM_PROMPT = """
        Eres "ROBOT IA", un asistente especializado EN REPARACIÓN Y TECNOLOGÍA creado ESPECÍFICAMENTE para esta app.
        Utilizas OLLAMA, tu propia IA de reparaciones integrada localmente en el dispositivo.
        
        ✅ TUS ESPECIALIDADES (RESPONDE SIEMPRE SOBRE ESTOS TEMAS):
        
        🛠️ REPARACIÓN DE HARDWARE:
        - Diagnóstico de problemas en laptops, desktops, tablets, celulares
        - Fallas de componentes: RAM, SSD, HDD, procesador, placa madre, fuente de poder
        - Problemas físicos: pantalla rota, batería dañada, cargador defectuoso
        - Conectores y puertos: USB, HDMI, Jack 3.5mm, puertos de carga
        - Periféricos: impresoras, scanners, monitores, teclados, ratones
        
        💾 PROBLEMAS DE SOFTWARE:
        - Drivers desactualizados o faltantes
        - Sistemas operativos: Windows, Linux, macOS, Android, iOS
        - Virus, malware, spyware - diagnóstico y eliminación
        - Optimización y limpieza de equipos
        - Actualizaciones y parches de seguridad
        - Programas que no funcionan o dan errores
        
        🌐 CONECTIVIDAD Y REDES:
        - WiFi lento o sin conexión
        - Problemas de Bluetooth
        - Configuración de red
        - Cables de red y conexiones
        
        ⚙️ MANTENIMIENTO PREVENTIVO:
        - Limpieza de polvo y ventiladores
        - Refrigeración de equipos
        - Cambio de pasta térmica
        - Reemplazo de componentes
        - Monitoreo de temperatura
        
        📊 RECOMENDACIONES TÉCNICAS:
        - Especificaciones de equipos
        - Compatibilidad de componentes
        - Compra de hardware recomendado
        - Valoración técnica de equipos
        
        ❌ DEBES RECHAZAR (NO SON TU ESPECIALIDAD):
        - Política, deportes, películas, comida, viajes, humor
        - Consejos médicos, legales, financieros
        - Contenido adulto o inapropiado
        - Cualquier tema NO relacionado con reparación/tecnología (excepto saludos cortos)
        
        🤖 TU IDENTIDAD:
        Si te preguntan quién eres: "Soy ROBOT IA, tu asistente especializado en reparación y tecnología. Utilizo OLLAMA, una inteligencia artificial propia integrada en esta app. Puedo ayudarte a diagnosticar y resolver problemas en laptops, desktops, impresoras, celulares y otros equipos electrónicos."
        
        📋 INSTRUCCIONES DE RESPUESTA:
        1. Sé técnico pero comprensible (nivel principiante a avanzado)
        2. Usa emojis técnicos: 💻 🔧 ⚙️ 🖥️ 📱 🖨️ 🔌 ⚡ 🛠️ 💾
        3. Respuestas breves y directas (2-3 párrafos máximo)
        4. Estructura: Problema → Diagnóstico → Solución
        5. Si necesitas más info, pregunta específicamente
        6. Si no sabes, admítelo honestamente
        7. Para problemas complejos, sugiere pasos detallados
        
        ⚠️ PARA PREGUNTAS FUERA DE ALCANCE:
        Responde SIEMPRE así: "❌ Lo siento, solo puedo ayudarte con reparación y tecnología. ¿Tienes algún problema técnico que pueda resolver?"
        
        🎯 RECUERDA: Tu único propósito es ayudar con reparaciones y problemas tecnológicos.
    """.trimIndent()

    private val palabrasClaveTecnologia = setOf(
        // Hardware general
        "laptop", "computadora", "pc", "desktop", "tablet", "smartphone", "celular", "móvil",
        "ipad", "iphone", "samsung", "xiaomi", "lenovo", "asus", "hp", "dell", "acer",

        // Problemas comunes
        "reparación", "arreglar", "problema", "error", "no funciona", "falla", "roto", "dañado",
        "lentitud", "lento", "se congela", "se cuelga", "se reinicia", "apaga", "no enciende",
        "no carga", "no abre", "no conecta", "desconecta", "lag", "retrasos",

        // Periféricos
        "impresora", "scanner", "monitor", "teclado", "ratón", "mouse", "webcam", "micrófono",
        "auriculares", "headphones", "parlante", "bocina", "router", "modem",

        // Componentes internos
        "ram", "ssd", "hdd", "disco duro", "procesador", "cpu", "gpu", "tarjeta gráfica",
        "placa madre", "motherboard", "fuente de poder", "psu", "ventilador", "disipador",
        "pasta térmica", "batería", "cargador", "adaptador", "cable",

        // Software y SO
        "driver", "drivers", "windows", "linux", "mac", "macos", "android", "ios",
        "sistema operativo", "so", "bios", "uefi", "firmware", "actualizaciones",

        // Seguridad
        "virus", "malware", "spyware", "antivirus", "seguridad", "contraseña", "cifrado",
        "hackeo", "piratería", "protección", "firewall", "defender", "mcafee", "avast",

        // Conectividad
        "wifi", "wifi", "bluetooth", "internet", "conexión", "red", "ethernet", "cable",
        "puerto", "usb", "hdmi", "jack", "adapter", "inalámbrico", "conexión lenta",

        // Energía y refrigeración
        "batería", "cargador", "voltaje", "electricidad", "calor", "temperatura", "frío",
        "refrigeración", "ventilación", "sobrecalentamiento", "overclocking",

        // Pantalla y gráficos
        "pantalla", "display", "monitor", "resolución", "gráficos", "video", "pixeles",
        "brillo", "contraste", "color", "refresh", "hdmi", "vga", "displayport",

        // Audio
        "audio", "sonido", "micrófono", "bocina", "parlante", "volumen", "mudo",

        // Almacenamiento
        "disco", "almacenamiento", "espacio", "capacidad", "partición", "formato", "borrar",
        "recuperación", "datos", "backup", "copia seguridad", "nube",

        // Mantenimiento
        "limpieza", "polvo", "mantenimiento", "optimización", "optim", "caché", "temporal",
        "desinstalar", "programa", "aplicación", "app", "software",

        // Compatibilidad
        "compatible", "incompatible", "especificaciones", "specs", "requerimientos",
        "comprar", "precio", "upgrade", "actualización", "mejora", "recomendación",

        // Conectores específicos
        "puerto", "conector", "adaptador", "dongle", "usb-c", "thunderbolt", "esim",

        // Servidores y virtuales
        "servidor", "máquina virtual", "vm", "virtualización", "virtual box", "vmware"
    )

    private val palabrasSaludos = setOf(
        "hola", "buenos días", "buenas tardes", "buenas noches", "buenos días",
        "¿cómo estás", "cómo estás", "qué tal", "hola!", "hey", "ei",
        "¿quién eres", "quién eres", "qué eres", "cuéntame de ti",
        "gracias", "muchas gracias", "ok", "está bien", "perfecto"
    )

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
        agregarMensaje("🤖 ¡Hola! Soy ROBOT IA, tu asistente especializado en reparación y tecnología 💻 ¿En qué puedo ayudarte hoy?", false)

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
        if (!esMensajeValido(mensaje)) {
            agregarMensaje(mensaje, true)
            etMensaje.text.clear()
            agregarMensaje("❌ Lo siento, solo puedo ayudarte con reparación y tecnología. ¿Tienes algún problema técnico que pueda resolver?", false)
            return
        }

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

    private fun esMensajeValido(mensaje: String): Boolean {
        val mensajeLower = mensaje.lowercase()

        // Acepta saludos
        val esSaludo = palabrasSaludos.any {
            mensajeLower.contains(it)
        }

        // Acepta preguntas técnicas
        val esTecnologia = palabrasClaveTecnologia.count {
            mensajeLower.contains(it)
        } > 0

        return esSaludo || esTecnologia
    }

    private fun esPrefiuntaTecnologica(mensaje: String): Boolean {
        val mensajeLower = mensaje.lowercase()
        val palabrasEncontradas = palabrasClaveTecnologia.count {
            mensajeLower.contains(it)
        }
        return palabrasEncontradas > 0
    }
}

// Modelo de mensaje
data class ChatMessage(
    val texto: String,
    val esUsuario: Boolean
)
