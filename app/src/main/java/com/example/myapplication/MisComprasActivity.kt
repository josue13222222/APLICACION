package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MisComprasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CompraAdapter
    private lateinit var listaCompras: List<Compra>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_compras)

        recyclerView = findViewById(R.id.recyclerMisCompras)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Ahora monto es Double (sin prefijo "S/")
        listaCompras = listOf(
            Compra("01/05/2025", "#123456", 85.00, "Entregado"),
            Compra("27/04/2025", "#123457", 120.00, "En camino"),
            Compra("25/04/2025", "#123458", 60.00, "Cancelado")
        )

        adapter = CompraAdapter(listaCompras)
        recyclerView.adapter = adapter
    }
}
