package com.example.readapplication.viewmodel

sealed class QuizEvent {
    object RepeatQuestion : QuizEvent()
    object SkipQuestion : QuizEvent()
}
