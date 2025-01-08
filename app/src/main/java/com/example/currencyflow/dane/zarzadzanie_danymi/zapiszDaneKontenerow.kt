package com.example.currencyflow.dane.zarzadzanie_danymi

import android.content.Context
import com.example.currencyflow.dane.C
import com.example.currencyflow.dane.ModelDanychKontenerow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File

fun zapiszDaneKontenerow(context: Context, kontenery: List<C>) {
    // Mapowanie kontenerów, usuwając "result"
    val konteneryDoZapisania = kontenery.map { kontener ->
        kontener.copy(result = "")
    }

    // PairDataModel
    val modelDanychKontenerow = ModelDanychKontenerow(konteneryDoZapisania.size, konteneryDoZapisania)

    // Serializacja obiektu PairDataModel do JSON
    val ciagJson = Json.encodeToString(modelDanychKontenerow)

    // Zapis do pliku
    val plik = File(context.filesDir, "liczba_kontenerow.json")
    plik.writeText(ciagJson)
}


