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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var selectedItem by remember { mutableStateOf("Нажми «Крутить»!") }
    
    var targetAngle by remember { mutableStateOf(0f) }
    val currentAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = tween(
            durationMillis = 3000,
            easing = CubicBezierEasing(0.15f, 0.85f, 0.15f, 1.0f)
        ),
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Magic Wheel",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedItem,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Box(
            modifier = Modifier
                .size(240.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(currentAngle)
            ) {
                val sliceAngle = 360f / items.value.size
                for (i in items.value.indices) {
                    drawArc(
                        color = colors[i % colors.size],
                        startAngle = i * sliceAngle,
                        sweepAngle = sliceAngle,
                        useCenter = true,
                        style = Fill
                    )
                }
            }

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

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp)
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
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newItemText.isNotBlank()) {
                                items.value.add(newItemText.trim())
                                newItemText = ""
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Добавить")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                            if (items.value.size > 2) {
                                TextButton(onClick = { items.value.removeAt(index) }) {
                                    Text("Удалить", color = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                if (items.value.isNotEmpty()) {
                    val randomSpins = Random.nextInt(3, 7) * 360f
                    val extraAngle = Random.nextFloat() * 360f
                    targetAngle += randomSpins + extraAngle

                    val normalizedAngle = (360f - (targetAngle % 360f) + 90f) % 360f
                    val sliceAngle = 360f / items.value.size
                    val index = ((normalizedAngle / sliceAngle).toInt()) % items.value.size
                    selectedItem = "Выпало: ${items.value[index]}"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Крутить колесо", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
