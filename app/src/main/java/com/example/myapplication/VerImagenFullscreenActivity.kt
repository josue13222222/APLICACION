package com.example.myapplication

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class VerImagenFullscreenActivity : AppCompatActivity() {

    private lateinit var imgFullscreen: ImageView
    private lateinit var tvTituloImagen: TextView
    private lateinit var gestureDetector: GestureDetector
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    private var escala = 1f
    private var escalaPinch = 1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_imagen_fullscreen)

        imgFullscreen = findViewById(R.id.imgFullscreen)
        tvTituloImagen = findViewById(R.id.tvTituloImagen)

        val imagenBase64 = intent.getStringExtra("imagen_base64") ?: ""
        val titulo = intent.getStringExtra("titulo") ?: "Imagen"

        tvTituloImagen.text = titulo

        if (imagenBase64.isNotEmpty()) {
            try {
                val decodedBytes = Base64.decode(imagenBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                imgFullscreen.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                escala = if (escala == 1f) 2f else 1f
                imgFullscreen.scaleX = escala
                imgFullscreen.scaleY = escala
                return true
            }
        })

        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                escalaPinch *= detector.scaleFactor
                escalaPinch = escalaPinch.coerceIn(1f, 5f)
                imgFullscreen.scaleX = escalaPinch
                imgFullscreen.scaleY = escalaPinch
                return true
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return gestureDetector.onTouchEvent(event)
    }
}
