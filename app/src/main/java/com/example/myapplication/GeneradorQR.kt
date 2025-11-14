package com.example.myapplication

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object GeneradorQR {
    fun generarQR(contenido: String, tamaño: Int = 512): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(contenido, BarcodeFormat.QR_CODE, tamaño, tamaño)
            val ancho = bitMatrix.width
            val alto = bitMatrix.height
            val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.RGB_565)

            for (x in 0 until ancho) {
                for (y in 0 until alto) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Generar QR con monto y número de teléfono
    fun generarQRPago(monto: Double, numeroContacto: String): Bitmap? {
        // Formato simplificado para QR de pago
        val contenido = "00020126360014br.gov.bcb.brcode010436${numeroContacto}52040000530340510${String.format("%.2f", monto)}5802PE6304000"
        return generarQR(contenido)
    }
}
