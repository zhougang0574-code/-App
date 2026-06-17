package com.example.readapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readapplication.model.AnswerResult
import com.example.readapplication.ui.theme.*

@Composable
fun ResultScreen(
    correctCount: Int,
    totalCount: Int,
    answers: List<AnswerResult>,
    onRestart: () -> Unit,
    onChangeDifficulty: () -> Unit
) {
    val stars = when {
        correctCount >= totalCount -> 3
        correctCount >= totalCount * 0.7 -> 2
        correctCount >= totalCount * 0.4 -> 1
        else -> 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCream)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "🎉", fontSize = 72.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "本轮结束！",
            style = MaterialTheme.typography.headlineLarge,
            color = DeepNavy
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "⭐".repeat(stars) + "☆".repeat(3 - stars),
            fontSize = 48.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "答对 $correctCount / $totalCount 题",
            style = MaterialTheme.typography.headlineMedium,
            color = if (correctCount >= totalCount * 0.7) CorrectGreen else CoralRed
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(answers) { result ->
                AnswerResultRow(result)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onChangeDifficulty,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(52.dp)
            ) {
                Text("换难度", style = MaterialTheme.typography.titleLarge)
            }
            Button(
                onClick = onRestart,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
            ) {
                Text("再来一次 🚀", style = MaterialTheme.typography.titleLarge, color = SunYellow)
            }
        }
    }
}

@Composable
private fun AnswerResultRow(result: AnswerResult) {
    val isCorrect = result.isCorrect
    val bgColor = if (isCorrect) CorrectGreen.copy(alpha = 0.1f) else WrongRed.copy(alpha = 0.08f)
    val icon = when {
        isCorrect -> "✅"
        result.isTimeout -> "⏰"
        else -> "❌"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${result.question.num1} ${result.question.operator.symbol} ${result.question.num2}",
            style = MaterialTheme.typography.titleLarge,
            color = DeepNavy
        )
        Text(
            text = "= ${result.question.answer}",
            style = MaterialTheme.typography.titleLarge,
            color = if (isCorrect) CorrectGreen else WrongRed
        )
        Text(text = icon, fontSize = 24.sp)
    }
}
