package com.example.myapplication

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminEmpenosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var spinnerEstado: Spinner
    private lateinit var btnFiltrar: Button
    private lateinit var btnAprobar: Button
    private lateinit var btnRechazar: Button
    private lateinit var txtTotalEmpenos: TextView
    private lateinit var txtPendientes: TextView
    private lateinit var txtAprobados: TextView

    private var empenosList = mutableListOf<Empeno>()
    private lateinit var empenosAdapter: EmpenosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_empenos)

        db = FirebaseFirestore.getInstance()
        initViews()
        setupSpinner()
        setupRecyclerView()
        loadEmpenos()
        loadEstadisticas()

        btnFiltrar.setOnClickListener { filtrarEmpenos() }
        btnAprobar.setOnClickListener { cambiarEstadoEmpeno("aprobado") }
        btnRechazar.setOnClickListener { cambiarEstadoEmpeno("rechazado") }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerEmpenos)
        spinnerEstado = findViewById(R.id.spinnerEstadoEmpeno)
        btnFiltrar = findViewById(R.id.btnFiltrarEmpenos)
        btnAprobar = findViewById(R.id.btnAprobarEmpeno)
        btnRechazar = findViewById(R.id.btnRechazarEmpeno)
        txtTotalEmpenos = findViewById(R.id.txtTotalEmpenos)
        txtPendientes = findViewById(R.id.txtEmpenosPendientes)
        txtAprobados = findViewById(R.id.txtEmpenosAprobados)
    }

    private fun setupSpinner() {
        val estados = arrayOf("Todos", "pendiente", "aprobado", "rechazado", "finalizado")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, estados)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEstado.adapter = adapter
    }

    private fun setupRecyclerView() {
        empenosAdapter = EmpenosAdapter(this, empenosList) { empeno ->
            actualizarBotonesEstado(empeno)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = empenosAdapter
    }

    private fun loadEmpenos() {
        db.collection("empenos")
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                empenosList.clear()
                for (document in documents) {
                    val empeno = document.toObject(Empeno::class.java)
                    empeno.id = document.id
                    empenosList.add(empeno)
                }
                empenosAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar empeños: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadEstadisticas() {
        db.collection("empenos").get()
            .addOnSuccessListener { documents ->
                val total = documents.size()
                var pendientes = 0
                var aprobados = 0

                for (document in documents) {
                    when (document.getString("estado")) {
                        "pendiente" -> pendientes++
                        "aprobado" -> aprobados++
                    }
                }

                txtTotalEmpenos.text = "Total: $total"
                txtPendientes.text = "Pendientes: $pendientes"
                txtAprobados.text = "Aprobados: $aprobados"
            }
    }

    private fun filtrarEmpenos() {
        val estadoSeleccionado = spinnerEstado.selectedItem.toString()

        if (estadoSeleccionado == "Todos") {
            loadEmpenos()
            return
        }

        db.collection("empenos")
            .whereEqualTo("estado", estadoSeleccionado)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                empenosList.clear()
                for (document in documents) {
                    val empeno = document.toObject(Empeno::class.java)
                    empeno.id = document.id
                    empenosList.add(empeno)
                }
                empenosAdapter.notifyDataSetChanged()
            }
    }

    private fun cambiarEstadoEmpeno(nuevoEstado: String) {
        val empenoSeleccionado = empenosAdapter.getSelectedEmpeno()
        if (empenoSeleccionado == null) {
            Toast.makeText(this, "Selecciona un empeño primero", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("empenos").document(empenoSeleccionado.id)
            .update("estado", nuevoEstado)
            .addOnSuccessListener {
                Toast.makeText(this, "Empeño $nuevoEstado exitosamente", Toast.LENGTH_SHORT).show()
                loadEmpenos()
                loadEstadisticas()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarBotonesEstado(empeno: Empeno) {
        btnAprobar.isEnabled = empeno.estado == "pendiente"
        btnRechazar.isEnabled = empeno.estado == "pendiente"
    }
}
