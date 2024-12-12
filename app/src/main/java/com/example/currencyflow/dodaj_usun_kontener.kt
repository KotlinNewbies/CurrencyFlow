package com.example.currencyflow

import android.util.Log
import androidx.activity.ComponentActivity
import com.example.currencyflow.klasy.Waluta
import com.example.currencyflow.data.C
import com.example.currencyflow.data.zarzadzanie_danymi.zapiszDaneKontenerow
import com.example.currencyflow.data.zarzadzanie_danymi.zapiszWybraneWaluty

fun dodajKontener(kontenery: MutableList<C>, ulubioneWaluty: List<Waluta>) {
    val pierwszaUlubionaWaluta = ulubioneWaluty.firstOrNull()
    val ostatniaUlubionaWaluta = ulubioneWaluty.last()

    if (pierwszaUlubionaWaluta != null) {
        kontenery.add(C(pierwszaUlubionaWaluta, ostatniaUlubionaWaluta, "", ""))
        //Log.d("Dodawanie Kontenera", "Dodano nowy kontener dla waluty: $pierwszaUlubionaWaluta")
    } else {
        Log.d("Dodawanie Kontenera", "Lista ulubionych walut jest pusta.")
        kontenery.add(C(Waluta.EUR, Waluta.USD, "", ""))
    }
}
fun dodajKontenerJesliBrak(kontenery: MutableList<C>, ulubioneWaluty: List<Waluta>, activity: ComponentActivity) {
        if (ulubioneWaluty.isEmpty() && kontenery.size < 1) {
            val domyslneWaluty = listOf(Waluta.EUR, Waluta.USD)
            kontenery.add(C(Waluta.EUR, Waluta.USD, "", ""))
            zapiszWybraneWaluty(activity, domyslneWaluty)
            zapiszDaneKontenerow(activity, kontenery)
        }
}

fun przywrocInterfejs(kontenery: MutableList<C>, from: Waluta, to: Waluta, amount: String, result: String) {
    kontenery.add(C(from, to, amount, result))
}

fun usunWybranyKontener(indeksKontenera: Int, kontenery: MutableList<C>, activity: ComponentActivity) {
    //Log.d("Usuwanie kontenera", "Usuwanie kontenera o indeksie: $indeksKontenera")
    if (indeksKontenera >= 0 && indeksKontenera < kontenery.size) {
        kontenery.removeAt(indeksKontenera)
        zapiszDaneKontenerow(activity, kontenery)
        //Log.d("Stan kontenerów", "Stan kontenerów po usunięciu: ${kontenery.size}")
    }
}
