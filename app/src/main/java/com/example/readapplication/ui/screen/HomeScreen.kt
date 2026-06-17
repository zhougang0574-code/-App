package com.example.readapplication.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readapplication.ui.theme.DeepNavy
import com.example.readapplication.ui.theme.SkyBlue
import com.example.readapplication.ui.theme.SunYellow

@Composable
fun HomeScreen(
    isInitializing: Boolean,
    onStart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(SkyBlue, SunYellow))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "⭐",
                fontSize = 96.sp
            )

            Text(
                text = "小算星",
                style = MaterialTheme.typography.displayMedium,
                color = DeepNavy,
                textAlign = TextAlign.Center
            )

            Text(
                text = "和小朋友一起学算数！",
                style = MaterialTheme.typography.titleLarge,
                color = DeepNavy.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isInitializing) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = DeepNavy)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("准备中…", color = DeepNavy)
                }
            } else {
                PulsingButton(onClick = onStart)
            }
        }
    }
}

@Composable
private fun PulsingButton(onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    Button(
        onClick = onClick,
        modifier = Modifier
            .scale(scale)
            .height(64.dp)
            .widthIn(min = 200.dp),
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
    ) {
        Text(
            text = "开始学习 🚀",
            style = MaterialTheme.typography.headlineMedium,
            color = SunYellow
        )
    }
}
