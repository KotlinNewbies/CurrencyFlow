package com.example.currencyflow.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat

fun triggerHardVibration(context: Context) {
    val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java) ?: return

    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
            val vibrationEffect = VibrationEffect.createOneShot(10L, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }
        else -> {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10L)
        }
    }
}
