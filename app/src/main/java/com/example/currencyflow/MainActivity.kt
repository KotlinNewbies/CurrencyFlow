package com.example.currencyflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.currencyflow.ui.theme.CurrencyFlowTheme
import com.example.currencyflow.ui.theme.loadData
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CurrencyFlowTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(this@MainActivity) // Przekazujemy kontekst com.example.template.MainActivity do funkcji com.example.template.Screen
                }
            }
        }
        // Sprawdzenie czy plik istnieje przy starcie aplikacji
        val fileName = "user_data.json"
        val file = File(filesDir, fileName)
        if (!file.exists()) {
            saveData(this) // Zapisz plik jeśli plik nie istnieje
        }
    }
}

@Composable
fun MainScreen(activity: ComponentActivity) {
    var response by remember { mutableStateOf("") }
    var elapsedTime by remember { mutableLongStateOf(0L) } // przechowywanie czasu
    val uuidString = loadData(activity)?.id ?: UUIDManager.getUUID()
    var networkError by remember { mutableStateOf(false) }
    var rcSuccess:Boolean by remember { mutableStateOf(false) }
    var dbSuccess:Boolean by remember { mutableStateOf(false) }

    // zmienne pól tekstowych
    var var1 by remember { mutableStateOf("")}
    var var1filling by remember { mutableStateOf("") }

    val pacificoRegular = FontFamily(
        Font(R.font.pacifico_regular, FontWeight.Bold)
    )

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "CurrencyFlow", fontFamily = pacificoRegular, fontSize = 35.sp)
        }
        Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(250.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .width(140.dp),
                value = "",
                onValueChange ={} )
            Icon( modifier = Modifier.size(40.dp),
                painter = painterResource(id = R.drawable.swap_horizontal),
                contentDescription = null)
            OutlinedTextField(
                modifier = Modifier
                    .width(140.dp),
                value = "",
                onValueChange ={} )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CurrencyFlowTheme {
        MainScreen(ComponentActivity())
    }
}