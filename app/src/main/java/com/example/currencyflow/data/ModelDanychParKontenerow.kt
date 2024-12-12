package com.example.currencyflow.data

import kotlinx.serialization.Serializable

@Serializable
data class ModelDanychParKontenerow(
    var liczbaKontenerow: Int,
    var kontenery: List<C>
)
