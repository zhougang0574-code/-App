package com.example.readapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readapplication.model.Difficulty
import com.example.readapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultyScreen(
    onSelect: (Difficulty) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择难度", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmCream)
            )
        },
        containerColor = WarmCream
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "选一个难度，开始答题吧！",
                style = MaterialTheme.typography.titleLarge,
                color = DeepNavy,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            val cardColors = listOf(MintGreen, SunYellow, CoralRed)
            Difficulty.entries.forEachIndexed { index, difficulty ->
                DifficultyCard(
                    difficulty = difficulty,
                    color = cardColors[index],
                    onClick = { onSelect(difficulty) }
                )
            }
        }
    }
}

@Composable
private fun DifficultyCard(
    difficulty: Difficulty,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = difficulty.emoji, fontSize = 48.sp)
            Column {
                Text(
                    text = difficulty.label,
                    style = MaterialTheme.typography.headlineMedium,
                    color = DeepNavy
                )
                Text(
                    text = difficulty.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = DeepNavy.copy(alpha = 0.7f)
                )
            }
        }
    }
}
