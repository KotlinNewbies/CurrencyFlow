package com.example.currencyflow.data.data_management

import android.content.Context
import com.example.currencyflow.data.C
import com.example.currencyflow.data.PairDataModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File

fun saveContainerData(context: Context, containers: List<C>) {
    // Mapowanie kontenerów, usuwając "result"
    val containersToSave = containers.map { container ->
        container.copy(result = "")
    }

    // PairDataModel
    val pairCountModel = PairDataModel(containersToSave.size, containersToSave)

    // Serializacja obiektu PairDataModel do JSON
    val jsonString = Json.encodeToString(pairCountModel)

    // Zapis do pliku
    val file = File(context.filesDir, "pair_count.json")
    file.writeText(jsonString)
}


