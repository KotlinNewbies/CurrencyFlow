package com.example.currencyflow.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ModelDanychKontenerow(
    var liczbaKontenerow: Int,
    var kontenery: List<C>
)
