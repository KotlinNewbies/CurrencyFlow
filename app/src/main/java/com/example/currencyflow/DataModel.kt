package com.example.currencyflow

import kotlinx.serialization.Serializable

@Serializable
data class DataModel(
    val id: String,
    val app: String,
    val v: String,
    //val list: String,
)

@Serializable
data class C(
    val from: String,
    val to: String,
    val amount: String,
)
