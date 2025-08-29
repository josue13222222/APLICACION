package com.example.myapplication

// importa Compra si está en otro paquete, sino no hace falta
import com.example.myapplication.Cupon

// uso de Cupon aquí

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class CuponesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cupones)

        val compras = listOf(
            Compra("01/05/2024", "ORD001", 250.0, "Completado"),
            Compra("15/05/2024", "ORD002", 120.0, "Completado"),
            Compra("28/05/2024", "ORD003", 80.0, "Completado")
        )

        val puntos = calcularPuntos(compras)
        val saldoSoles = calcularValorEnSoles(puntos)

        val tvPuntos = findViewById<TextView>(R.id.tvPuntos)
        tvPuntos.text = "Puntos acumulados: $puntos (S/. ${"%.2f".format(saldoSoles)})"

        val cupones = listOf(
            Cupon("Descuento 10%", "Descuento", "ABC123", "01/01/2025", "Activo"),
            Cupon("Envío Gratis", "Envío Gratuito", "XYZ456", "30/06/2025", "Activo")
        )

        val recyclerView: RecyclerView = findViewById(R.id.rvCupones)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CuponesAdapter(this, cupones)

        val filtros = listOf("Todos", "Descuento", "Envío Gratuito")
        val spinner = findViewById<Spinner>(R.id.spinnerFiltro)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filtros)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun calcularPuntos(compras: List<Compra>): Int {
        return compras.sumOf { (it.total / 100).toInt() }
    }

    private fun calcularValorEnSoles(puntos: Int): Double {
        return puntos * 0.5
    }
}
