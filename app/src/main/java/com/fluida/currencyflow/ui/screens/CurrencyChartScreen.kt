package com.fluida.currencyflow.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
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
import androidx.compose.ui.graphics.drawscope.clipRect
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
    viewModel: CurrencyHistoryViewModel,
) {
    val historia by viewModel.historia.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDays by viewModel.selectedDays.collectAsState()
    val quicksand = FontFamily(Font(R.font.quicksand_variable, FontWeight.Normal))
    val textMeasurer = rememberTextMeasurer()
    val config = LocalConfiguration.current
    val locale = remember(config) { config.locales[0] ?: Locale.getDefault() }

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
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.rounded_arrow_back_24),
                            contentDescription = null,
                            modifier = Modifier.padding(horizontal = 8.dp).size(16.dp).graphicsLayer(rotationZ = 180f),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        walutaTo?.let {
                            Image(
                                painter = painterResource(id = it.icon),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "$fromSymbol / $toSymbol",
                            fontFamily = quicksand,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.rounded_arrow_back_24),
                            contentDescription = UiText.StringResource(R.string.action_back).asString(),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
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
                RangeButton(UiText.StringResource(R.string.chart_range_7d).asString(), 7, selectedDays) { viewModel.loadHistory(fromSymbol, toSymbol, 7) }
                RangeButton(UiText.StringResource(R.string.chart_range_1m).asString(), 30, selectedDays) { viewModel.loadHistory(fromSymbol, toSymbol, 30) }
                RangeButton(UiText.StringResource(R.string.chart_range_6m).asString(), 180, selectedDays) { viewModel.loadHistory(fromSymbol, toSymbol, 180) }
                RangeButton(UiText.StringResource(R.string.chart_range_1y).asString(), 365, selectedDays) { viewModel.loadHistory(fromSymbol, toSymbol, 365) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (historia.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(UiText.StringResource(R.string.chart_no_data).asString(), fontFamily = quicksand)
                }
            } else {
                var touchedPoint by remember { mutableStateOf<HistorycznyKurs?>(null) }

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
                        locale = locale,
                    ) { touchedPoint = it }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.widthIn(max = 240.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(84.dp)
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (touchedPoint != null) {
                                    try {
                                        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                        val outputFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", locale)
                                        LocalDateTime.parse(touchedPoint!!.date, inputFormatter).format(outputFormatter)
                                    } catch (_: Exception) {
                                        touchedPoint!!.date
                                    }
                                } else {
                                    UiText.StringResource(R.string.chart_last_rate).asString()
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = quicksand,
                                color = Color(0xFFFFD700),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(locale, "%.4f", touchedPoint?.value ?: historia.last().value),
                                style = MaterialTheme.typography.headlineMedium,
                                fontFamily = quicksand,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700),
                                maxLines = 1
                            )
                        }
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
    locale: Locale,
    onPointTouched: (HistorycznyKurs?) -> Unit
) {
    val sortedPoints = remember(points) { points.sortedBy { it.date } }
    val isPositiveTrend = (sortedPoints.size >= 2) && (sortedPoints.last().value >= sortedPoints.first().value)
    val chartColor = if (isPositiveTrend) Color(0xFF22C55E) else Color(0xFFEF4444)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val labelStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = fontFamily
    )

    val progress = remember { Animatable(0f) }
    LaunchedEffect(sortedPoints) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "dotPulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseProgress"
    )

    var touchedIndex by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(sortedPoints) {
                if (sortedPoints.isEmpty()) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val chartWidth = size.width.toFloat() - 16.dp.toPx()
                    val stepX = if (sortedPoints.size > 1) chartWidth / (sortedPoints.size - 1) else 1f
                    val index = if (stepX > 0f) (down.position.x / stepX).roundToInt().coerceIn(0, sortedPoints.size - 1) else 0
                    touchedIndex = index
                    onPointTouched(sortedPoints[index])

                    horizontalDrag(down.id) { change ->
                        val dragChartWidth = size.width.toFloat() - 16.dp.toPx()
                        val dragStepX = if (sortedPoints.size > 1) dragChartWidth / (sortedPoints.size - 1) else 1f
                        val dragIndex = if (dragStepX > 0f) (change.position.x / dragStepX).roundToInt().coerceIn(0, sortedPoints.size - 1) else 0
                        touchedIndex = dragIndex
                        onPointTouched(sortedPoints[dragIndex])
                    }
                    touchedIndex = null
                    onPointTouched(null)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (sortedPoints.isEmpty()) return@Canvas
            
            val actualMin = sortedPoints.minOf { it.value }
            val actualMax = sortedPoints.maxOf { it.value }
            
            // Dodaj 15% marginesu pionowego, żeby linia nie była na samej górze/dole
            val valuePadding = (actualMax - actualMin) * 0.15
            val minVal = (actualMin - valuePadding).let { if ((it < 0) && (actualMin >= 0)) 0.0 else it }
            val maxVal = actualMax + valuePadding
            val range = (maxVal - minVal).let { if (it == 0.0) 1.0 else it }
            
            val fullWidth = size.width
            val rightMargin = 16.dp.toPx()
            val chartWidth = fullWidth - rightMargin
            // Zmniejszamy wysokość wykresu o 28dp, aby zrobić bezpieczną przestrzeń na etykiety dat na dole
            val height = size.height - 28.dp.toPx()
            
            // Rysowanie siatki poziomej (3 linie) na pełną szerokość
            for (i in 0..2) {
                val yGrid = (height * i) / 2f
                drawLine(
                    color = gridColor,
                    start = Offset(0f, yGrid),
                    end = Offset(fullWidth, yGrid),
                    strokeWidth = 1.dp.toPx()
                )
                
                // Etykiety Y
                val labelValue = maxVal - ((range * i) / 2f)
                val labelText = String.format(locale, "%.3f", labelValue)
                val labelLayout = textMeasurer.measure(labelText, labelStyle)
                val yOffset = if (i == 2) {
                    // Dla dolnej linii rysujemy etykietę Y POWYŻEJ linii, żeby nie nachodziła na datę pod wykresem
                    yGrid - labelLayout.size.height - 3.dp.toPx()
                } else {
                    yGrid + 3.dp.toPx()
                }
                drawText(
                    textMeasurer = textMeasurer,
                    text = labelText,
                    style = labelStyle,
                    topLeft = Offset(4.dp.toPx(), yOffset)
                )
            }
            
            if (sortedPoints.size < 2) {
                // Jeśli tylko jeden punkt, narysuj kropkę na środku
                drawCircle(
                    color = chartColor,
                    radius = 6.dp.toPx(),
                    center = Offset(chartWidth / 2, height / 2)
                )
                return@Canvas
            }
            
            val stepX = chartWidth / (sortedPoints.size - 1)
            
            val coordinates = sortedPoints.mapIndexed { index, point ->
                Offset(
                    x = index * stepX,
                    y = height - ((((point.value - minVal) / range) * height)).toFloat()
                )
            }
            
            val path = Path().apply {
                moveTo(coordinates.first().x, coordinates.first().y)
            }
            val fillPath = Path().apply {
                moveTo(coordinates.first().x, height)
                lineTo(coordinates.first().x, coordinates.first().y)
            }
            
            for (i in 0 until (coordinates.size - 1)) {
                val p0 = coordinates[i]
                val p1 = coordinates[i + 1]
                val controlPoint1 = Offset(p0.x + ((p1.x - p0.x) / 2f), p0.y)
                val controlPoint2 = Offset(p0.x + ((p1.x - p0.x) / 2f), p1.y)
                
                path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                fillPath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
            }
            
            fillPath.lineTo(coordinates.last().x, height)
            fillPath.close()
            
            // Rysowanie z animacją clipRect (płynne rysowanie od lewej do prawej)
            clipRect(
                left = 0f,
                top = 0f,
                right = fullWidth * progress.value,
                bottom = size.height
            ) {
                // Rysowanie wypełnienia (gradient)
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(chartColor.copy(alpha = 0.35f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    ),
                    style = Fill
                )
                
                // Rysowanie głównej wygładzonej linii
                drawPath(
                    path = path,
                    color = chartColor,
                    style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
                
                // Kropka wyróżniająca ostatni punkt na wykresie (pojawia się pod koniec animacji)
                if ((progress.value > 0.95f) && (touchedIndex == null)) {
                    val lastPoint = coordinates.last()
                    
                    // Efekt "oddechu" (pulsowanie) jak w klawiaturach
                    val pulseRadius = 5.dp.toPx() + (10.dp.toPx() * pulseProgress)
                    val pulseAlpha = 0.7f * (1f - pulseProgress)
                    drawCircle(
                        color = chartColor.copy(alpha = pulseAlpha),
                        radius = pulseRadius,
                        center = lastPoint
                    )
                    
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = lastPoint
                    )
                    drawCircle(
                        color = chartColor,
                        radius = 3.5.dp.toPx(),
                        center = lastPoint
                    )
                }
            }

            // Kursor dotykowy (crosshair i punkt pod palcem)
            touchedIndex?.let { idx ->
                if (idx in coordinates.indices) {
                    val pt = coordinates[idx]
                    drawLine(
                        color = chartColor.copy(alpha = 0.8f),
                        start = Offset(pt.x, 0f),
                        end = Offset(pt.x, height),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = chartColor,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                }
            }
            
            // Etykiety X (Timeline dynamiczny: początek, środek i koniec)
            try {
                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val outputFormatter = DateTimeFormatter.ofPattern("d MMM", locale)
                
                val firstDate = LocalDateTime.parse(sortedPoints.first().date, inputFormatter).format(outputFormatter)
                val middleDate = LocalDateTime.parse(sortedPoints[sortedPoints.size / 2].date, inputFormatter).format(outputFormatter)
                val lastDate = LocalDateTime.parse(sortedPoints.last().date, inputFormatter).format(outputFormatter)

                val firstLayout = textMeasurer.measure(firstDate, labelStyle)
                val midLayout = textMeasurer.measure(middleDate, labelStyle)
                val endLayout = textMeasurer.measure(lastDate, labelStyle)

                val yDate = size.height - firstLayout.size.height

                // Lewa etykieta (Początek)
                drawText(
                    textMeasurer = textMeasurer,
                    text = firstDate,
                    style = labelStyle,
                    topLeft = Offset(0f, yDate)
                )
                
                // Środkowa etykieta
                drawText(
                    textMeasurer = textMeasurer,
                    text = middleDate,
                    style = labelStyle,
                    topLeft = Offset((chartWidth / 2f) - (midLayout.size.width / 2f), yDate)
                )
                
                // Prawa etykieta (Koniec)
                drawText(
                    textMeasurer = textMeasurer,
                    text = lastDate,
                    style = labelStyle,
                    topLeft = Offset(chartWidth - endLayout.size.width, yDate)
                )
            } catch (_: Exception) {
                // W razie błędu formatowania daty, pomiń etykiety
            }
        }
    }
}
