package com.fsag.magicwheel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF6750A4),
                    background = Color(0xFF1C1B1F),
                    surface = Color(0xFF2B2930),
                    onSurface = Color(0xFFE6E1E5)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WheelScreen()
                }
            }
        }
    }
}

@Composable
fun WheelScreen() {
    var items = remember { mutableStateOf(mutableStateListOf("Пицца", "Суши", "Бургер", "Шаурма", "WOK", "Паста")) }
    var newItemText by remember { mutableStateOf("") }

    val colors = listOf(
        Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6),
        Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFF4DB6AC),
        Color(0xFFF06292), Color(0xFFFFD54F), Color(0xFF4DD0E1)
    )

    var selectedItem by remember { mutableStateOf("") }
    var isSpinning by remember { mutableStateOf(false) }
    
    var targetAngle by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    val currentAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = tween(
            durationMillis = 6000, // Увеличили время до 6 секунд для долгой интриги
            easing = CubicBezierEasing(0.05f, 0.9f, 0.1f, 1.0f)
        ),
        finishedListener = {
            isSpinning = false
            if (items.value.isNotEmpty()) {
                val normalizedAngle = (360f - (targetAngle % 360f) + 90f) % 360f
                val sliceAngle = 360f / items.value.size
                val index = ((normalizedAngle / sliceAngle).toInt()) % items.value.size
                selectedItem = "Выпало: ${items.value[index]}"
            }
        },
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Приподнятый заголовок без дублирования
        Text(
            text = "Magic Wheel",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )

        // Блок результата (показывается только после остановки для интриги)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().height(55.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSpinning) "Колесо крутится..." else (if (selectedItem.isEmpty()) "Нажми «Крутить колесо»" else selectedItem),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSpinning) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Колесо с текстом внутри и стрелочкой сверху
        Box(
            modifier = Modifier
                .size(230.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(currentAngle)
            ) {
                val sliceAngle = 360f / items.value.size
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                for (i in items.value.indices) {
                    val startAngle = i * sliceAngle
                    drawArc(
                        color = colors[i % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sliceAngle,
                        useCenter = true,
                        style = Fill
                    )

                    // Отрисовка текста внутри секторов колеса
                    drawContext.canvas.nativeCanvas.apply {
                        val angleRad = Math.toRadians((startAngle + sliceAngle / 2f).toDouble())
                        val textRadius = radius * 0.6f
                        val x = center.x + (textRadius * cos(angleRad)).toFloat()
                        val y = center.y + (textRadius * sin(angleRad)).toFloat()

                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 32f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                            setShadowLayer(4f, 1f, 1f, android.graphics.Color.BLACK)
                        }

                        // Рисуем обрезанный текст, если название длинное
                        val text = items.value[i]
                        val displaytext = if (text.length > 8) text.take(7) + "…" else text
                        
                        save()
                        translate(x, y)
                        rotate((startAngle + sliceAngle / 2f + 90f), 0f, 0f)
                        drawText(displaytext, 0f, 0f, paint)
                        restore()
                    }
                }
            }

            // Красивая красная стрелочка-указатель сверху
            Canvas(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopCenter)
            ) {
                val path = Path().apply {
                    moveTo(size.width / 2f, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(path = path, color = Color.Red)
            }
        }

        // Блок управления списком (блокируется при вращении через цвета и клики)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        label = { Text("Новый вариант") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !isSpinning // Блокируем поле ввода
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newItemText.isNotBlank() && !isSpinning) {
                                items.value.add(newItemText.trim())
                                newItemText = ""
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSpinning // Блокируем кнопку добавления
                    ) {
                        Text("Добавить")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(items.value) { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "• $item", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                            if (items.value.size > 2 && !isSpinning) {
                                TextButton(onClick = { items.value.removeAt(index) }) {
                                    Text("Удалить", color = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Кнопка запуска (светлеет и блокируется во время вращения)
        Button(
            onClick = {
                if (items.value.isNotEmpty() && !isSpinning) {
                    isSpinning = true
                    selectedItem = ""
                    // Больше оборотов (от 8 до 12 полных кругов) для долгой сильной прокрутки
                    val randomSpins = Random.nextInt(8, 13) * 360f
                    val extraAngle = Random.nextFloat() * 360f
                    targetAngle += randomSpins + extraAngle
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) // Делаем светлее/бледнее при блокировке
            ),
            enabled = !isSpinning,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isSpinning) "Крутим..." else "Крутить колесо",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSpinning) Color.White.copy(alpha = 0.6f) else Color.White
            )
        }
    }
}
