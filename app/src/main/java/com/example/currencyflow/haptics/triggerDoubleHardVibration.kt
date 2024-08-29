package com.example.currencyflow.haptics

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat

fun triggerDoubleHardVibration(context: Context) {
    val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java) ?: return

    val timings = longArrayOf(0, 70, 50, 70) // Czasy w milisekundach: opóźnienie, wibracja, opóźnienie, wibracja
    val amplitudes = intArrayOf(0, 80, 0, 80) // Amplitudy: 0 - brak wibracji, 255 - maksymalna wibracja

    val vibrationEffect = VibrationEffect.createWaveform(timings, amplitudes, -1) // -1 oznacza powtórzenie raz
    vibrator.vibrate(vibrationEffect)
}