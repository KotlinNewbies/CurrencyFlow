package com.example.currencyflow.data.zarzadzanie_danymi

import android.content.Context
import com.example.currencyflow.data.C
import com.example.currencyflow.data.ModelDanychParKontenerow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File

fun zapiszDaneKontenerow(context: Context, kontenery: List<C>) {
    // Mapowanie kontenerów, usuwając "result"
    val konteneryDoZapisania = kontenery.map { kontener ->
        kontener.copy(result = "")
    }

    // PairDataModel
    val modelDanychParKontenerow = ModelDanychParKontenerow(konteneryDoZapisania.size, konteneryDoZapisania)

    // Serializacja obiektu PairDataModel do JSON
    val ciagJson = Json.encodeToString(modelDanychParKontenerow)

    // Zapis do pliku
    val plik = File(context.filesDir, "liczba_kontenerow.json")
    plik.writeText(ciagJson)
}


