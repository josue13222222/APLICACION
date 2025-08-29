package com.example.myapplication

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class DetalleProductoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_producto)

        val nombre = intent.getStringExtra("nombre")
        val precio = intent.getStringExtra("precio")
        val imagen = intent.getIntExtra("imagen", 0)

        val tvNombre: TextView = findViewById(R.id.tvNombreProducto)
        val tvPrecio: TextView = findViewById(R.id.tvPrecioProducto)
        val ivImagen: ImageView = findViewById(R.id.ivProducto)

        tvNombre.text = nombre
        tvPrecio.text = precio
        ivImagen.setImageResource(imagen)
    }
}
