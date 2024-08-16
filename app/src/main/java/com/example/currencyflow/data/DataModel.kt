package com.example.currencyflow.data

import com.example.currencyflow.classes.Currency
import kotlinx.serialization.Serializable

@Serializable
data class DataModel(
    val id: String,
    val app: String,
    val v: String,
)

@Serializable
data class C(
    val from: Currency,
    val to: Currency,
    val amount: String,
    val result: String,
)

@Serializable
data class Conversion(
    val amount: Double,
    val from: String,
    val to: String,
    val value: Double // Zmiana z String na Double
)

