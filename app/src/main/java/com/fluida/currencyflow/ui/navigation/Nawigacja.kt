package com.fluida.currencyflow.ui.navigation

enum class Nawigacja(val route: String) {
    Dom("dom"),
    UlubioneWaluty("ulubione"),
    Ustawenia("ustawienia"),
    Login("login"),
    Register("register"),
    Historia("historia/{from}/{to}");

    companion object {
        fun createHistoriaRoute(from: String, to: String) = "historia/$from/$to"
    }
}
