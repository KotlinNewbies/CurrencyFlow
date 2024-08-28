package com.example.currencyflow.haptics

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat

fun triggerSoftVibration(context: Context) {
    val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java) ?: return

    val vibrationEffect = VibrationEffect.createOneShot(5L, VibrationEffect.DEFAULT_AMPLITUDE)
    vibrator.vibrate(vibrationEffect)
}
