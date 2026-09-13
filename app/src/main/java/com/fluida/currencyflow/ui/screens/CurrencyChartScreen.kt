package com.fluida.currencyflow.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fluida.currencyflow.R
import com.fluida.currencyflow.data.model.HistorycznyKurs
import com.fluida.currencyflow.data.model.Waluta
import com.fluida.currencyflow.viewmodel.CurrencyHistoryViewModel
import com.fluida.currencyflow.util.UiText
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyChartScreen(
    fromSymbol: String,
    toSymbol: String,
    navController: NavController,
    viewModel: CurrencyHistoryViewModel
) {
    val historia by viewModel.historia.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDays by viewModel.selectedDays.collectAsState()
    val quicksand = FontFamily(Font(R.font.quicksand_variable, FontWeight.Normal))
    val textMeasurer = rememberTextMeasurer()
    val config = LocalConfiguration.current
    val locale = config.locales[0] ?: Locale.getDefault()

    val walutaFrom = remember(fromSymbol) { Waluta.entries.find { it.symbol == fromSymbol } }
    val walutaTo = remember(toSymbol) { Waluta.entries.find { it.symbol == toSymbol } }

    LaunchedEffect(fromSymbol, toSymbol) {
        viewModel.loadHistory(fromSymbol, toSymbol, 7)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        walutaFrom?.let {
                            Image(
                                painter = painterResource(id = it.icon),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.rounded_arrow_back_24),
                            contentDescription = null,
                            modifier = Modifier.padding(horizontal = 8.dp).size(16.dp).graphicsLayer(rotationZ = 180f),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        walutaTo?.let {
                            Image(
                                painter = painterResource(id = it.icon),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "$fromSymbol / $toSymbol",
                            fontFamily = quicksand,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.rounded_arrow_back_24),
                            contentDescription = "Wstecz",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Przycisk zakresu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RangeButton("7d", 7, selectedDays) { viewModel.loadHistory(fromSymbol, toSymbol, 7) }
                RangeButton("1m", 30, selectedDays) { viewModel.loadHistory(fromSymbol, toSymbol, 30) }
                RangeButton("6m", 180, selectedDays) { viewModel.loadHistory(fromSymbol, toSymbol, 180) }
                RangeButton("1r", 365, selectedDays) { viewModel.loadHistory(fromSymbol, toSymbol, 365) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (historia.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("Brak danych historycznych", fontFamily = quicksand)
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 32.dp) // Zwiększony padding, żeby etykiety dat nie były ucięte
                ) {
                    LineChart(
                        points = historia,
                        textMeasurer = textMeasurer,
                        fontFamily = quicksand,
                        locale = locale
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (historia.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ostatni kurs",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = quicksand
                        )
                        Text(
                            text = String.format(locale, "%.4f", historia.last().value),
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = quicksand,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RangeButton(text: String, days: Int, selectedDays: Int, onClick: () -> Unit) {
    val isSelected = days == selectedDays
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.height(40.dp)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LineChart(
    points: List<HistorycznyKurs>,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    fontFamily: FontFamily,
    locale: Locale
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val labelStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        fontSize = 10.sp,
        fontFamily = fontFamily
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (points.isEmpty()) return@Canvas
        
        val actualMin = points.minOf { it.value }
        val actualMax = points.maxOf { it.value }
        
        // Dodaj 10% marginesu pionowego, żeby linia nie była na samej górze/dole
        val valuePadding = (actualMax - actualMin) * 0.15
        val minVal = (actualMin - valuePadding).let { if (it < 0 && actualMin >= 0) 0.0 else it }
        val maxVal = actualMax + valuePadding
        val range = (maxVal - minVal).let { if (it == 0.0) 1.0 else it }
        
        val width = size.width
        // Zmniejszamy wysokość wykresu o 24dp, aby zrobić bezpieczną przestrzeń na etykiety dat na dole
        val height = size.height - 24.dp.toPx()
        
        // Rysowanie siatki poziomej (3 linie)
        for (i in 0..2) {
            val yGrid = height * i / 2f
            drawLine(
                color = gridColor,
                start = Offset(0f, yGrid),
                end = Offset(width, yGrid),
                strokeWidth = 1.dp.toPx()
            )
            
            // Etykiety Y
            val labelValue = maxVal - (range * i / 2f)
            drawText(
                textMeasurer = textMeasurer,
                text = String.format(locale, "%.3f", labelValue),
                style = labelStyle,
                topLeft = Offset(4.dp.toPx(), yGrid + 2.dp.toPx())
            )
        }
        
        if (points.size < 2) {
            // Jeśli tylko jeden punkt, narysuj kropkę na środku
            drawCircle(
                color = primaryColor,
                radius = 6.dp.toPx(),
                center = Offset(width / 2, height / 2)
            )
            return@Canvas
        }
        
        val stepX = width / (points.size - 1)
        
        val path = Path()
        val fillPath = Path()
        
        points.forEachIndexed { index, point ->
            val x = index * stepX
            val y = height - ((point.value - minVal) / range * height).toFloat()
            
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            
            if (index == points.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }
        
        // Rysowanie wypełnienia (gradient)
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent),
                startY = 0f,
                endY = height
            ),
            style = Fill
        )
        
        // Rysowanie głównej linii
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Etykiety X (Timeline dynamiczny: początek, środek i koniec)
        try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val outputFormatter = DateTimeFormatter.ofPattern("d MMM", locale)
            
            val firstDate = LocalDateTime.parse(points.first().date, inputFormatter).format(outputFormatter)
            val middleDate = LocalDateTime.parse(points[points.size / 2].date, inputFormatter).format(outputFormatter)
            val lastDate = LocalDateTime.parse(points.last().date, inputFormatter).format(outputFormatter)

            // Lewa etykieta (Początek)
            drawText(
                textMeasurer = textMeasurer,
                text = firstDate,
                style = labelStyle,
                topLeft = Offset(0f, size.height - 16.dp.toPx())
            )
            
            // Środkowa etykieta
            val midLayout = textMeasurer.measure(middleDate, labelStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = middleDate,
                style = labelStyle,
                topLeft = Offset((width / 2f) - (midLayout.size.width / 2f), size.height - 16.dp.toPx())
            )
            
            // Prawa etykieta (Koniec)
            val endLayout = textMeasurer.measure(lastDate, labelStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = lastDate,
                style = labelStyle,
                topLeft = Offset(width - endLayout.size.width, size.height - 16.dp.toPx())
            )
        } catch (_: Exception) {
            // W razie błędu formatowania daty, pomiń etykiety
        }
    }
}
