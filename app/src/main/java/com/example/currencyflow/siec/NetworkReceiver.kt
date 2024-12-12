//package com.example.currencyflow.siec
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//
//class NetworkReceiver(private val onNetworkAvailable: () -> Unit) : BroadcastReceiver() {
//    override fun onReceive(context: Context?, intent: Intent?) {
//        if (isInternetAvailable(context ?: return)) {
//            onNetworkAvailable()
//        }
//    }
//}
