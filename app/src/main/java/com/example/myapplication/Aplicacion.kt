package com.example.myapplication

// 🔴 Firebase activado
import android.app.Application
import com.google.firebase.FirebaseApp

internal class Aplicacion : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            FirebaseApp.initializeApp(this) // 🔥 FirebaseApp inicializado
        } catch (e: Exception) {
            e.printStackTrace() // 🛠️ Imprime detalles en Logcat
        }
    }
}

