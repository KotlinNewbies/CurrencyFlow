package com.example.currencyflow.data

import com.example.currencyflow.classes.Currency
import kotlinx.serialization.Serializable
import java.math.BigDecimal

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
    @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
    val from: String,
    val to: String,
    @Serializable(with = BigDecimalSerializer::class) val value: BigDecimal,
)

