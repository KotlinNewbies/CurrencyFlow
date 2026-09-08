package com.fluida.currencyflow.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val favoriteCurrencies: List<Waluta>,
    val containers: List<C>,
    val languageTag: String,
    val decimalPlaces: Int
)
