package com.example.myapplication

import android.content.Context

object SistemaPuntos {
    private const val PREFS_NAME = "mis_puntos"
    private const val KEY_PUNTOS = "puntos"

    private var puntosTotales: Int = 0

    fun cargarPuntos(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        puntosTotales = prefs.getInt(KEY_PUNTOS, 0)
    }

    fun guardarPuntos(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_PUNTOS, puntosTotales).apply()
    }

    fun agregarPuntos(context: Context, montoCompra: Double) {
        val puntosGanados = (montoCompra / 100).toInt()
        puntosTotales += puntosGanados
        guardarPuntos(context)
    }

    fun usarPuntos(context: Context, puntos: Int): Boolean {
        return if (puntosTotales >= puntos) {
            puntosTotales -= puntos
            guardarPuntos(context)
            true
        } else {
            false
        }
    }

    fun obtenerPuntos(): Int = puntosTotales

    fun obtenerDescuento(puntos: Int): Double = puntos * 0.50
}
