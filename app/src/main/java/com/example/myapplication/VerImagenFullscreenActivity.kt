package com.example.myapplication

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class VerImagenFullscreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_imagen_fullscreen)

        val imagenBase64 = intent.getStringExtra("imagen_base64") ?: ""
        val imageView = findViewById<ImageView>(R.id.imgFullscreen)

        if (imagenBase64.isNotEmpty()) {
            try {
                val decodedBytes = Base64.decode(imagenBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
