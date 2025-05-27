//package com.example.currencyflow.siec
//
//import android.content.Context
//import android.net.ConnectivityManager
//import android.net.NetworkCapabilities
//
//fun sprawdzDostepnoscInternetu(context: Context): Boolean {
//    val menadzerLacznosci = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//    val siec = menadzerLacznosci.activeNetwork ?: return false
//    val mozliwosci = menadzerLacznosci.getNetworkCapabilities(siec) ?: return false
//    return mozliwosci.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
//            mozliwosci.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
//}
