package com.example.currencyflow.data

import kotlinx.serialization.Serializable

@Serializable
data class PairDataModel(
    var pairCount: Int,
    var containers: List<C>
)
