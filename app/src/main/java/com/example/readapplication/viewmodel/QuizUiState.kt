package com.example.readapplication.viewmodel

import com.example.readapplication.model.AnswerResult
import com.example.readapplication.model.Difficulty
import com.example.readapplication.model.Question
import com.example.readapplication.speech.SttManager

data class QuizUiState(
    val isInitializing: Boolean = true,
    val sttStatus: SttManager.Status = SttManager.Status.READY,
    val phase: QuizPhase = QuizPhase.IDLE,
    val difficulty: Difficulty? = null,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val retryCount: Int = 0,
    val answers: List<AnswerResult> = emptyList(),
    val timeLeft: Int = QUESTION_TIMEOUT,
    val recognizedText: String = ""
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)
    val totalQuestions: Int get() = questions.size
    val correctCount: Int get() = answers.count { it.isCorrect }
    val questionNumber: Int get() = currentIndex + 1

    companion object {
        const val QUESTION_TIMEOUT = 10
        const val MAX_RETRIES = 1
    }
}
