package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mas)

        val btnAdmin = findViewById<Button>(R.id.btnAdmin)
        btnAdmin.setOnClickListener {
            startActivity(Intent(this, AdminPanelActivity::class.java))
        }
    }
}
