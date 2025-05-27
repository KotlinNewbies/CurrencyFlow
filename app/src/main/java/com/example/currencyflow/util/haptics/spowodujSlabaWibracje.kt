package com.example.currencyflow.util.haptics

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat

fun spowodujSlabaWibracje(context: Context) {
    val wibracja = ContextCompat.getSystemService(context, Vibrator::class.java) ?: return

    val efektWibracji = VibrationEffect.createOneShot(5L, VibrationEffect.DEFAULT_AMPLITUDE)
    wibracja.vibrate(efektWibracji)
}
