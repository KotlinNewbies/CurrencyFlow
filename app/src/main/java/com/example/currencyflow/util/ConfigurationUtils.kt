package com.example.currencyflow.util

import android.content.res.Configuration

/**
 * Sprawdza, czy bieżąca konfiguracja ekranu odpowiada rozmiarowi typowemu dla telefonu.
 * @param configuration Konfiguracja urządzenia, zwykle pobierana z LocalConfiguration.current.
 * @return True, jeśli rozmiar ekranu to SCREENLAYOUT_SIZE_NORMAL, w przeciwnym razie false.
 */
fun isDeviceProbablyPhone(configuration: Configuration): Boolean {
    val screenLayoutInt = configuration.screenLayout
    return screenLayoutInt and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_NORMAL
}