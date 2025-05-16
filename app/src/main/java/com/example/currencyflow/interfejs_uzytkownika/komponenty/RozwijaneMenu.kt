package com.example.currencyflow.interfejs_uzytkownika.komponenty

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.currencyflow.klasy.Waluta

@Composable
fun RozwijaneMenu(
    wybranaWaluta: Waluta,
    zdarzenieWybraniaWaluty: (Waluta) -> Unit,
    wybraneWaluty: List<Waluta>, // Zawsze używaj tej listy do wyświetlania opcji
) {
    var rozwiniety by remember { mutableStateOf(false) }

    // Logowanie otrzymanych propsów dla debugowania
    Log.d("RozwijaneMenu", "Renderuję. Wybrana: ${wybranaWaluta.symbol}, Dostępne (props): ${wybraneWaluty.map { it.symbol }}")

    // Użyj LaunchedEffect do obsługi sytuacji, gdy wybranaWaluta nie jest już dostępna.
    // Ten efekt zostanie uruchomiony, gdy wybranaWaluta lub wybraneWaluty się zmienią.
    LaunchedEffect(wybranaWaluta, wybraneWaluty) {
        Log.d("RozwijaneMenu", "LaunchedEffect: Wybrana: ${wybranaWaluta.symbol}, Dostępne: ${wybraneWaluty.map { it.symbol }}")
        if (wybraneWaluty.isNotEmpty() && !wybraneWaluty.contains(wybranaWaluta)) {
            Log.d("RozwijaneMenu", "Wybrana waluta ${wybranaWaluta.symbol} nie jest na liście dostępnych. Wybieram pierwszą dostępną.")
            //zdarzenieWybraniaWaluty(wybraneWaluty.first())
        } else if (wybraneWaluty.isEmpty()) {
            // Co zrobić, jeśli lista dostępnych walut jest pusta, a jakaś waluta jest "wybrana"?
            // To zależy od logiki aplikacji. Może HomeViewModel powinien zapewnić,
            // że jeśli `dostepneWalutyDlaKontenerow` jest puste, to kontenery też
            // mają jakieś "puste" lub specjalne oznaczenie waluty.
            // Na razie załóżmy, że HomeViewModel dba o to, by `wybranaWaluta` była sensowna
            // lub że pusty `DropdownMenu` jest akceptowalny.
            Log.d("RozwijaneMenu", "Lista dostępnych walut jest pusta.")
        }
    }

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopEnd)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clickable {
                    if (wybraneWaluty.isNotEmpty()) { // Pozwól otworzyć tylko, jeśli są opcje
                        rozwiniety = !rozwiniety
                    } else {
                        Log.d(
                            "RozwijaneMenu",
                            "Brak dostępnych walut, menu nie zostanie rozwinięte."
                        )
                        // Można dodać np. Toast informujący użytkownika
                    }
                },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Pokaż ikonę tylko jeśli `wybraneWaluty` nie jest puste
                // lub jeśli `wybranaWaluta` jest jakąś sensowną wartością domyślną
                // Nawet jeśli `wybraneWaluty` jest puste, `wybranaWaluta` (z kontenera) nadal tu będzie.
                // Ikona powinna być zawsze widoczna, jeśli kontener ma jakąś walutę.
                Image(
                    modifier = Modifier.size(50.dp),
                    painter = painterResource(id = wybranaWaluta.icon),
                    contentDescription = wybranaWaluta.symbol
                )
            }
        }

        DropdownMenu(
            modifier = Modifier
                .heightIn(max = 400.dp) // Ogranicz wysokość menu
                .background(MaterialTheme.colorScheme.onBackground),
            expanded = rozwiniety,
            onDismissRequest = { rozwiniety = false }
        ) {
            // ZAWSZE iteruj po `wybraneWaluty` (props) do budowania menu.
            // Jeśli `wybraneWaluty` jest puste, menu będzie puste - co jest poprawne.
            if (wybraneWaluty.isEmpty()) {
                Log.d("RozwijaneMenu", "DropdownMenu: Brak walut do wyświetlenia w menu.")
                // Możesz opcjonalnie dodać DropdownMenuItem z informacją "Brak dostępnych walut"
                // DropdownMenuItem(
                // text = { Text("Brak dostępnych walut") },
                // onClick = { rozwiniety = false },
                // enabled = false
                // )
            } else {
                Log.d("RozwijaneMenu", "DropdownMenu: Wyświetlam waluty: ${wybraneWaluty.map { it.symbol }}")
                wybraneWaluty.forEach { currency ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    modifier = Modifier.size(26.dp),
                                    painter = painterResource(id = currency.icon),
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = currency.symbol, color = MaterialTheme.colorScheme.surface) // Ustaw kolor tekstu
                            }
                        },
                        onClick = {
                            Log.d("RozwijaneMenu", "KLIKNIĘTO: ${currency.symbol}. Aktualnie wybrana: ${wybranaWaluta.symbol}")
                            if (wybranaWaluta != currency) {
                                Log.d("RozwijaneMenu", "Wywołuję zdarzenieWybraniaWaluty z ${currency.symbol}")
                                zdarzenieWybraniaWaluty(currency)
                            } else {
                                Log.d("RozwijaneMenu", "Waluta ${currency.symbol} jest już wybrana, nie wywołuję zdarzenia.")
                            }
                            rozwiniety = false
                        }
                    )
                }
            }
        }
    }
}
