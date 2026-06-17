package com.example.readapplication.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.readapplication.speech.SttManager
import com.example.readapplication.ui.theme.*
import com.example.readapplication.viewmodel.QuizEvent
import com.example.readapplication.viewmodel.QuizPhase
import com.example.readapplication.viewmodel.QuizUiState

@Composable
fun QuizScreen(
    uiState: QuizUiState,
    onEvent: (QuizEvent) -> Unit,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasAudioPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasAudioPermission) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(uiState.phase) {
        if (uiState.phase == QuizPhase.FINISHED) onFinished()
    }

    val bgColor by animateColorAsState(
        targetValue = when (uiState.phase) {
            QuizPhase.FEEDBACK -> if (uiState.answers.lastOrNull()?.isCorrect == true) CorrectGreen.copy(alpha = 0.2f) else WrongRed.copy(alpha = 0.1f)
            else -> WarmCream
        },
        label = "bg"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QuizTopBar(
                current = uiState.questionNumber,
                total = uiState.totalQuestions,
                correct = uiState.correctCount
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.sttStatus == SttManager.Status.MODEL_MISSING) {
                ModelMissingBanner()
            }

            if (!hasAudioPermission) {
                PermissionBanner()
            }

            Spacer(modifier = Modifier.weight(1f))

            uiState.currentQuestion?.let { question ->
                Text(
                    text = question.toDisplayText(),
                    style = MaterialTheme.typography.displayLarge,
                    color = DeepNavy,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.recognizedText.isNotEmpty()) {
                Text(
                    text = "「 ${uiState.recognizedText} 」",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SkyBlue,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            CountdownTimer(
                timeLeft = uiState.timeLeft,
                total = QuizUiState.QUESTION_TIMEOUT,
                isActive = uiState.phase == QuizPhase.LISTENING
            )

            Spacer(modifier = Modifier.height(24.dp))

            PhaseIndicator(phase = uiState.phase)

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { onEvent(QuizEvent.RepeatQuestion) },
                    enabled = uiState.phase != QuizPhase.SPEAKING,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("🔁 重播", style = MaterialTheme.typography.labelLarge)
                }
                OutlinedButton(
                    onClick = { onEvent(QuizEvent.SkipQuestion) },
                    enabled = uiState.phase == QuizPhase.LISTENING,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("⏭ 跳过", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuizTopBar(current: Int, total: Int, correct: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "第 $current / $total 题",
            style = MaterialTheme.typography.titleLarge,
            color = DeepNavy
        )
        Text(
            text = "⭐ $correct",
            style = MaterialTheme.typography.titleLarge,
            color = DeepNavy
        )
    }
}

@Composable
private fun CountdownTimer(timeLeft: Int, total: Int, isActive: Boolean) {
    val progress = timeLeft.toFloat() / total
    val color = when {
        progress > 0.5f -> MintGreen
        progress > 0.25f -> SunYellow
        else -> TimerOrange
    }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.05f else 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "timerScale"
    )

    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .size(120.dp)
                .scale(scale),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            strokeWidth = 8.dp,
            strokeCap = StrokeCap.Round
        )
        Text(
            text = "$timeLeft",
            style = MaterialTheme.typography.headlineLarge,
            color = if (isActive) DeepNavy else DeepNavy.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun PhaseIndicator(phase: QuizPhase) {
    val (emoji, label, color) = when (phase) {
        QuizPhase.SPEAKING -> Triple("📢", "正在播报…", SkyBlue)
        QuizPhase.LISTENING -> Triple("🎤", "请说出答案", MintGreen)
        QuizPhase.FEEDBACK -> {
            val lastCorrect = phase == QuizPhase.FEEDBACK
            Triple("💬", "答题反馈中…", SunYellow)
        }
        else -> Triple("⏳", "请稍候…", DeepNavy.copy(alpha = 0.3f))
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(text = emoji, fontSize = 24.sp)
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = DeepNavy)
    }
}

@Composable
private fun ModelMissingBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WrongRed.copy(alpha = 0.1f))
            .padding(12.dp)
    ) {
        Text(
            text = "⚠️ 语音识别模型未找到，请将 vosk-model-small-cn 放入 assets 目录",
            style = MaterialTheme.typography.bodyLarge,
            color = WrongRed
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun PermissionBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SunYellow.copy(alpha = 0.3f))
            .padding(12.dp)
    ) {
        Text(
            text = "🎙 需要麦克风权限才能语音答题",
            style = MaterialTheme.typography.bodyLarge,
            color = DeepNavy
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}
