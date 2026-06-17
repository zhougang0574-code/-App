package com.example.readapplication.model

data class AnswerResult(
    val question: Question,
    val userAnswer: Int?,
    val isCorrect: Boolean,
    val isTimeout: Boolean = false
)
