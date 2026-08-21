package com.fluida.currencyflow.viewmodel.ads

// Stany, jakie może przyjąć baner reklamowy
sealed class AdBannerState {
    object Idle : AdBannerState() // Oczekuje na inicjalizację lub gotowy do nowej próby
    object Loading : AdBannerState()
    object Loaded : AdBannerState() // Reklama załadowana i gotowa do wyświetlenia
    object Disabled : AdBannerState() // Reklamy wyłączone (usługa premium)
    data class Error(val message: String, val errorCode: Int) : AdBannerState()
}

// Zdarzenia, które ViewModel może wysłać do Composable
sealed class AdBannerUiEvent {
    object LoadAd : AdBannerUiEvent()  // Nakazuje Composable załadować reklamę do jego AdView
    object ShowAd : AdBannerUiEvent()  // Nakazuje Composable pokazać (już załadowaną) reklamę
}
