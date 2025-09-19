package com.example.myapplication

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminCuponesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var etCodigoCupon: EditText
    private lateinit var etDescuentoPorcentaje: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var btnCrearCupon: Button
    private lateinit var btnEliminarCupon: Button
    private lateinit var txtTotalCupones: TextView
    private lateinit var txtCuponesActivos: TextView

    private var cuponesList = mutableListOf<Cupon>()
    private lateinit var cuponesAdapter: CuponesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_cupones)

        db = FirebaseFirestore.getInstance()
        initViews()
        setupRecyclerView()
        loadCupones()
        loadEstadisticas()

        btnCrearCupon.setOnClickListener { crearCupon() }
        btnEliminarCupon.setOnClickListener { eliminarCupon() }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerCupones)
        etCodigoCupon = findViewById(R.id.etCodigoCupon)
        etDescuentoPorcentaje = findViewById(R.id.etDescuentoPorcentaje)
        etDescripcion = findViewById(R.id.etDescripcionCupon)
        btnCrearCupon = findViewById(R.id.btnCrearCupon)
        btnEliminarCupon = findViewById(R.id.btnEliminarCupon)
        txtTotalCupones = findViewById(R.id.txtTotalCupones)
        txtCuponesActivos = findViewById(R.id.txtCuponesActivos)
    }

    private fun setupRecyclerView() {
        // ⬇️ Si tu adapter necesita Context, pásalo como primer parámetro:
        cuponesAdapter = CuponesAdapter(this, cuponesList) { cupon ->
            btnEliminarCupon.isEnabled = true
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = cuponesAdapter
    }

    private fun loadCupones() {
        db.collection("cupones")
            .get()
            .addOnSuccessListener { documents ->
                cuponesList.clear()
                for (document in documents) {
                    val cupon = document.toObject(Cupon::class.java)
                    cupon.id = document.id
                    cuponesList.add(cupon)
                }
                cuponesAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar cupones: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadEstadisticas() {
        db.collection("cupones").get()
            .addOnSuccessListener { documents ->
                val total = documents.size()
                var activos = 0

                for (document in documents) {
                    val activo = document.getBoolean("activo") ?: true
                    if (activo) activos++
                }

                txtTotalCupones.text = "Total: $total"
                txtCuponesActivos.text = "Activos: $activos"
            }
    }

    private fun crearCupon() {
        val codigo = etCodigoCupon.text.toString().trim()
        val descuentoStr = etDescuentoPorcentaje.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()

        if (codigo.isEmpty() || descuentoStr.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val descuento = descuentoStr.toDoubleOrNull()
        if (descuento == null || descuento <= 0 || descuento > 100) {
            Toast.makeText(this, "El descuento debe ser entre 1 y 100", Toast.LENGTH_SHORT).show()
            return
        }

        val cupon = hashMapOf(
            "codigo" to codigo.uppercase(),
            "descuentoPorcentaje" to descuento,
            "descripcion" to descripcion,
            "activo" to true,
            "fechaCreacion" to com.google.firebase.Timestamp.now(),
            "usos" to 0
        )

        db.collection("cupones")
            .add(cupon)
            .addOnSuccessListener {
                Toast.makeText(this, "Cupón creado exitosamente", Toast.LENGTH_SHORT).show()
                limpiarCampos()
                loadCupones()
                loadEstadisticas()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al crear cupón: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarCupon() {
        val cuponSeleccionado = cuponesAdapter.getSelectedCupon()
        if (cuponSeleccionado == null) {
            Toast.makeText(this, "Selecciona un cupón primero", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("cupones").document(cuponSeleccionado.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Cupón eliminado exitosamente", Toast.LENGTH_SHORT).show()
                loadCupones()
                loadEstadisticas()
                btnEliminarCupon.isEnabled = false
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al eliminar cupón: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun limpiarCampos() {
        etCodigoCupon.text.clear()
        etDescuentoPorcentaje.text.clear()
        etDescripcion.text.clear()
    }
}
