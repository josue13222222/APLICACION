package com.example.myapplication

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AdminEmpenosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var txtTotalEmpenos: TextView
    private lateinit var txtPendientes: TextView
    private lateinit var txtAprobados: TextView

    private var empenosList = mutableListOf<Empeno>()
    private lateinit var empenosAdapter: EmpenosPendientesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_empenos)

        db = FirebaseFirestore.getInstance()
        initViews()
        setupRecyclerView()
        loadEmpenosPendientes()
        loadEstadisticas()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerEmpenosPendientes)
        txtTotalEmpenos = findViewById(R.id.txtTotalEmpenos)
        txtPendientes = findViewById(R.id.txtEmpenosPendientes)
        txtAprobados = findViewById(R.id.txtEmpenosAprobados)
    }

    private fun setupRecyclerView() {
        empenosAdapter = EmpenosPendientesAdapter(this, empenosList,
            onAprobar = { empeno -> aprobarEmpeno(empeno) },
            onRechazar = { empeno -> rechazarEmpeno(empeno) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = empenosAdapter
    }

    private fun loadEmpenosPendientes() {
        db.collection("empenos")
            .whereEqualTo("estadoAprobacion", "pendiente")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                empenosList.clear()
                snapshots?.documents?.forEach { doc ->
                    val empeno = doc.toObject(Empeno::class.java)
                    empeno?.let { empenosList.add(it) }
                }

                empenosAdapter.notifyDataSetChanged()
            }
    }

    private fun loadEstadisticas() {
        db.collection("empenos").get()
            .addOnSuccessListener { documents ->
                val total = documents.size()
                var pendientes = 0
                var aprobados = 0

                for (document in documents) {
                    when (document.getString("estadoAprobacion")) {
                        "pendiente" -> pendientes++
                        "aprobado" -> aprobados++
                    }
                }

                txtTotalEmpenos.text = "Total: $total"
                txtPendientes.text = "Pendientes: $pendientes"
                txtAprobados.text = "Aprobados: $aprobados"
            }
    }

    private fun aprobarEmpeno(empeno: Empeno) {
        AlertDialog.Builder(this)
            .setTitle("Aprobar Empeño")
            .setMessage("¿Aprobar empeño de ${empeno.producto}?\n\nSe otorgarán ${empeno.puntos} puntos al usuario.")
            .setPositiveButton("Aprobar") { _, _ ->
                db.collection("empenos").document(empeno.id)
                    .update("estadoAprobacion", "aprobado")
                    .addOnSuccessListener {
                        db.collection("users").document(empeno.userId)
                            .update("points", FieldValue.increment(empeno.puntos.toLong()))
                            .addOnSuccessListener {
                                Toast.makeText(this, "Empeño aprobado. ${empeno.puntos} puntos otorgados", Toast.LENGTH_SHORT).show()
                                loadEmpenosPendientes()
                                loadEstadisticas()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error otorgando puntos: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error aprobando empeño: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun rechazarEmpeno(empeno: Empeno) {
        AlertDialog.Builder(this)
            .setTitle("Rechazar Empeño")
            .setMessage("¿Rechazar empeño de ${empeno.producto}?\n\nEl usuario NO recibirá puntos.")
            .setPositiveButton("Rechazar") { _, _ ->
                db.collection("empenos").document(empeno.id)
                    .update("estadoAprobacion", "rechazado")
                    .addOnSuccessListener {
                        Toast.makeText(this, "Empeño rechazado", Toast.LENGTH_SHORT).show()
                        loadEmpenosPendientes()
                        loadEstadisticas()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
