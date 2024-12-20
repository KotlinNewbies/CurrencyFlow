package com.example.currencyflow.dane

import kotlinx.serialization.Serializable

@Serializable
data class ModelDanychParKontenerow(
    var liczbaKontenerow: Int,
    var kontenery: List<C>
)
