package com.fluida.currencyflow.data.model

import kotlinx.serialization.Serializable

@Serializable
data class HistorycznyKurs(
    val value: Double,
    val date: String
)

@Serializable
data class HistoryResponse(
    val rcSuccess: Boolean,
    val symbol: String? = null,
    val history: List<HistorycznyKurs>? = null,
    val message: String? = null
)
