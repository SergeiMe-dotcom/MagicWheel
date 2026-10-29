package com.fsag.magicwheel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
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
    val items = listOf("Пицца", "Суши", "Бургер", "Шаурма", "WOK", "Паста")
    val colors = listOf(
        Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6),
        Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFF4DB6AC)
    )

    var selectedItem by remember { mutableStateOf("FSAG • Нажми «Крутить»!") }
    
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Magic Wheel",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "FSAG — Free Software and Games",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedItem,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Box(
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(currentAngle)
            ) {
                val sliceAngle = 360f / items.size
                for (i in items.indices) {
                    drawArc(
                        color = colors[i % colors.size],
                        startAngle = i * sliceAngle,
                        sweepAngle = sliceAngle,
                        useCenter = true,
                        style = Fill
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.TopCenter)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }

        Button(
            onClick = {
                val randomSpins = Random.nextInt(3, 7) * 360f
                val extraAngle = Random.nextFloat() * 360f
                targetAngle += randomSpins + extraAngle

                val normalizedAngle = (360f - (targetAngle % 360f) + 90f) % 360f
                val sliceAngle = 360f / items.size
                val index = ((normalizedAngle / sliceAngle).toInt()) % items.size
                selectedItem = "Выпало: ${items[index]}"
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "Крутить колесо", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
