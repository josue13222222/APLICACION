package com.example.myapplication

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    fun scheduleEmpenoDebtNotifications(context: Context) {
        val empenoDebtWork = PeriodicWorkRequestBuilder<EmpenoDebtNotificationWorker>(
            1, TimeUnit.DAYS // Ejecutar diariamente
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "empeno_debt_notifications",
            ExistingPeriodicWorkPolicy.KEEP,
            empenoDebtWork
        )
    }
}
