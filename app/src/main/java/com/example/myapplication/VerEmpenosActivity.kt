package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VerEmpenosActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val lista = mutableListOf<Empeno>()
    private lateinit var adapter: EmpenosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_empenos)

        val rv = findViewById<RecyclerView>(R.id.rvEmpenos)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = EmpenosAdapter(lista)
        rv.adapter = adapter

        val uid = auth.currentUser?.uid ?: return

        // Escucha en tiempo real los empeños del usuario
        db.collection("empenos")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                lista.clear()
                snapshot?.forEach { doc ->
                    val e = doc.toObject(Empeno::class.java)
                    lista.add(e)
                }
                adapter.notifyDataSetChanged()
            }
    }
}
