package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class NotificacionesActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val notificaciones = mutableListOf<Notificacion>()
    private lateinit var adapter: NotificacionesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NotificacionesAdapter(notificaciones)
        recyclerView.adapter = adapter

        val uid = auth.currentUser?.uid ?: return
        cargarNotificaciones(uid)
    }

    private fun cargarNotificaciones(uid: String) {
        db.collection("notificaciones")
            .whereEqualTo("userId", uid)
            .orderBy("fechaCreacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                notificaciones.clear()
                snapshot?.forEach { doc ->
                    val notif = doc.toObject(Notificacion::class.java)
                    notificaciones.add(notif)
                }
                adapter.notifyDataSetChanged()
            }
    }
}
