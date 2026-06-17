package com.example.readapplication.engine

import com.example.readapplication.model.Difficulty
import com.example.readapplication.model.Operator
import com.example.readapplication.model.Question

object QuestionGenerator {

    fun generate(difficulty: Difficulty, count: Int = 10): List<Question> =
        (1..count).map { generate(difficulty) }.toList()

    private fun generate(difficulty: Difficulty): Question {
        val useSubtraction = difficulty.allowSubtraction && (0..1).random() == 1
        return if (useSubtraction) generateSubtraction(difficulty.maxNumber)
        else generateAddition(difficulty.maxNumber)
    }

    private fun generateAddition(max: Int): Question {
        val num1 = (1 until max).random()
        val num2 = (1..(max - num1)).random()
        return Question(num1, num2, Operator.ADD, num1 + num2)
    }

    private fun generateSubtraction(max: Int): Question {
        val num1 = (2..max).random()
        val num2 = (1 until num1).random()
        return Question(num1, num2, Operator.SUBTRACT, num1 - num2)
    }
}
