package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminEmpenosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var txtTotalEmpenos: TextView
    private lateinit var txtActivos: TextView
    private lateinit var txtEliminados: TextView

    private var empenosList = mutableListOf<Empeno>()
    private lateinit var empenosAdapter: EmpenosPendientesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_empenos)

        db = FirebaseFirestore.getInstance()
        initViews()
        setupRecyclerView()
        loadEmpenosAprobados()
        loadEstadisticas()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerEmpenosPendientes)
        txtTotalEmpenos = findViewById(R.id.txtTotalEmpenos)
        txtActivos = findViewById(R.id.txtEmpenosPendientes)
        txtEliminados = findViewById(R.id.txtEmpenosAprobados)

        val btnRegistrarEmpenoAdmin = findViewById<Button>(R.id.btnRegistrarEmpenoAdmin)
        btnRegistrarEmpenoAdmin.setOnClickListener {
            val intent = Intent(this, AdminRegistrarEmpenoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        empenosAdapter = EmpenosPendientesAdapter(this, empenosList,
            onAprobar = { empeno -> eliminarEmpeno(empeno) },
            onRechazar = { empeno -> eliminarEmpeno(empeno) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = empenosAdapter
    }

    private fun loadEmpenosAprobados() {
        db.collection("empenos")
            .whereEqualTo("estado", "activo")
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
                var activos = 0
                var eliminados = 0

                for (document in documents) {
                    when (document.getString("estado")) {
                        "activo" -> activos++
                        "eliminado" -> eliminados++
                    }
                }

                txtTotalEmpenos.text = "Total: $total"
                txtActivos.text = "Activos: $activos"
                txtEliminados.text = "Eliminados: $eliminados"
            }
    }

    private fun eliminarEmpeno(empeno: Empeno) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Empeño")
            .setMessage("¿Eliminar empeño de ${empeno.producto}? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection("empenos").document(empeno.id)
                    .update("estado", "eliminado")
                    .addOnSuccessListener {
                        Toast.makeText(this, "Empeño eliminado", Toast.LENGTH_SHORT).show()
                        loadEmpenosAprobados()
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
