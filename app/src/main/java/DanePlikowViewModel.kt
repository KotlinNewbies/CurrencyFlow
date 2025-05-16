//package com.example.currencyflow.dane.zarzadzanie_danymi
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope // Potrzebne do uruchamiania korutyn
//import com.example.currencyflow.dane.ModelDanychKontenerow // Twój model danych do pliku
//import kotlinx.coroutines.flow.* // Potrzebne do StateFlow
//import kotlinx.coroutines.launch // Potrzebne do launch
//
//// ViewModel dedykowany do zarządzania danymi z plików
//class DanePlikowViewModel(
//    // Wstrzyknij zależność od repozytorium danych
//    private val repositoryData: RepositoryData
//) : ViewModel() {
//
//    // StateFlow do przechowywania wczytanych danych z pliku.
//    // Reprezentuje stan danych, który UI może obserwować.
//    // Jest nullable, bo dane mogą nie być jeszcze wczytane lub plik może nie istnieć.
//    private val _zapisaneDane = MutableStateFlow<ModelDanychKontenerow?>(null)
//    val zapisaneDane: StateFlow<ModelDanychKontenerow?> = _zapisaneDane.asStateFlow()
//
//    // StateFlow do sygnalizowania stanu ładowania
//    private val _ladowanieDanychPlikowych = MutableStateFlow(false)
//    val ladowanieDanychPlikowych: StateFlow<Boolean> = _ladowanieDanychPlikowych.asStateFlow()
//
//    // StateFlow do sygnalizowania wystąpienia błędu podczas operacji na plikach
//    private val _bladPlikowy = MutableStateFlow(false)
//    val bladPlikowy: StateFlow<Boolean> = _bladPlikowy.asStateFlow()
//
//    init {
//        // W bloku init uruchamiamy korutynę do wczytania danych z pliku przy starcie ViewModelu.
//        // Używamy viewModelScope, aby korutyna była powiązana z cyklem życia ViewModelu.
//        wczytajDaneZPliku() // Wywołujemy funkcję wczytującą
//    }
//
//    // Funkcja do wczytywania danych z pliku.
//    // Uruchamia korutynę i wywołuje funkcję suspend z repozytorium.
//    fun wczytajDaneZPliku() {
//        viewModelScope.launch { // Uruchom korutynę w viewModelScope
//            _ladowanieDanychPlikowych.value = true // Ustaw stan ładowania na true
//            _bladPlikowy.value = false // Resetuj stan błędu
//
//            try {
//                // Wywołaj funkcję suspend z repozytorium.
//                // withContext(Dispatchers.IO) w repozytorium zapewni wykonanie w wątku IO.
//                val dane = repositoryData.loadContainerData() // Wywołanie suspend fun
//
//                // Zaktualizuj StateFlow w ViewModelu po wczytaniu danych
//                _zapisaneDane.value = dane
//                _bladPlikowy.value = false // Sukces, brak błędu
//            } catch (e: Exception) {
//                // Obsługa błędów wczytywania
//                e.printStackTrace()
//                _bladPlikowy.value = true // Ustaw stan błędu
//                _zapisaneDane.value = null // W przypadku błędu dane są null
//            } finally {
//                _ladowanieDanychPlikowych.value = false // Zakończ ładowanie
//            }
//        }
//    }
//
//    // Funkcja do zapisywania danych do pliku.
//    // Przyjmuje dane jako argument i uruchamia korutynę do zapisu.
//    fun zapiszDaneDoPliku(dane: ModelDanychKontenerow) {
//        viewModelScope.launch { // Uruchom korutynę w viewModelScope
//            // Możesz dodać tutaj obsługę ładowania i błędów zapisu, jeśli potrzebujesz
//            try {
//                repositoryData.saveContainerData(dane) // Wywołanie suspend fun zapisu
//                // Opcjonalnie: zaktualizuj _zapisaneDane po zapisie, jeśli chcesz
//                _bladPlikowy.value = false // Sukces
//            } catch (e: Exception) {
//                e.printStackTrace()
//                _bladPlikowy.value = true // Błąd zapisu
//            }
//        }
//    }
//
//    // Funkcja do pobierania aktualnie wczytanych danych bez ponownego wczytywania z pliku
//    fun pobierzAktualneDane(): ModelDanychKontenerow? {
//        return _zapisaneDane.value
//    }
//}