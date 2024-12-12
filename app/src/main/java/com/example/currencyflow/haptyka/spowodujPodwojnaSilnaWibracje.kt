package com.example.currencyflow.haptyka

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat

fun spowodujPodwojnaSilnaWibracje(context: Context) {
    val wibracja = ContextCompat.getSystemService(context, Vibrator::class.java) ?: return

    val czasy = longArrayOf(0, 70, 50, 70) // Czasy w milisekundach: opóźnienie, wibracja, opóźnienie, wibracja
    val amplitudy = intArrayOf(0, 80, 0, 80) // Amplitudy: 0 - brak wibracji, 255 - maksymalna wibracja

    val efektWibracji = VibrationEffect.createWaveform(czasy, amplitudy, -1) // -1 oznacza jedno powtorzenie
    wibracja.vibrate(efektWibracji)
}