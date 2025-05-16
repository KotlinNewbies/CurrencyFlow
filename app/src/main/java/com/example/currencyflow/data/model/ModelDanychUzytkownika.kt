package com.example.currencyflow.data.model

import com.example.currencyflow.util.KonwerterLiczbDziesietnych
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class ModelDanychUzytkownika(
    val id: String,
    val app: String,
    val v: String,
)

@Serializable
data class C(
    val from: Waluta,
    val to: Waluta,
    val amount: String,
    val result: String,
)

@Serializable
data class Konwersja(
    @Serializable(with = KonwerterLiczbDziesietnych::class) val amount: BigDecimal,
    val from: String,
    val to: String,
    @Serializable(with = KonwerterLiczbDziesietnych::class) val value: BigDecimal,
)

