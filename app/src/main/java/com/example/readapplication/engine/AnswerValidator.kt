package com.example.readapplication.engine

import com.example.readapplication.model.Question

object AnswerValidator {
    fun check(question: Question, userAnswer: Int): Boolean = userAnswer == question.answer
}
