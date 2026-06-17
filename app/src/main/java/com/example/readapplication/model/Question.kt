package com.example.readapplication.model

data class Question(
    val num1: Int,
    val num2: Int,
    val operator: Operator,
    val answer: Int
) {
    fun toSpeechText(): String = "${num1} ${operator.speechWord} ${num2} 等于多少？"
    fun toDisplayText(): String = "${num1} ${operator.symbol} ${num2} = ?"
}
