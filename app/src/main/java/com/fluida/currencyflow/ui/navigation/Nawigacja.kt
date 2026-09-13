package com.fluida.currencyflow.ui.navigation

enum class Nawigacja(val route: String) {
    Dom("dom"),
    UlubioneWaluty("ulubione"),
    Ustawenia("ustawienia"),
    Login("login"),
    Register("register"),
    Historia("historia/{symbol}");

    companion object {
        fun createHistoriaRoute(symbol: String) = "historia/$symbol"
    }
}
