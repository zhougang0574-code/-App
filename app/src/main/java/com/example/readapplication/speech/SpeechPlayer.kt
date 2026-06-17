package com.example.readapplication.speech

import android.content.Context
import com.example.readapplication.model.Operator
import com.example.readapplication.model.Question

class SpeechPlayer(context: Context) {

    private val player = AudioPlayer(context)

    companion object {
        private const val BASE = "audio"
        private val CORRECT_CLIPS = listOf(
            "$BASE/correct_1.mp3",
            "$BASE/correct_2.mp3",
            "$BASE/correct_3.mp3"
        )
        private val RESULT_GREAT = "$BASE/result_great.mp3"
        private val RESULT_OK    = "$BASE/result_ok.mp3"
        private val RESULT_TRY   = "$BASE/result_try.mp3"
    }

    fun playQuestion(question: Question, onDone: () -> Unit) {
        player.playSequence(
            listOf(num(question.num1), op(question.operator), num(question.num2), "$BASE/q_suffix.mp3"),
            onDone
        )
    }

    fun playCorrect(onDone: () -> Unit) {
        player.playSequence(listOf(CORRECT_CLIPS.random()), onDone)
    }

    fun playRetry(onDone: () -> Unit) {
        player.playSequence(listOf("$BASE/retry.mp3"), onDone)
    }

    /** 答错且无重试机会：播"答错啦，答案是" + 数字 */
    fun playWrongAnswer(answer: Int, onDone: () -> Unit) {
        player.playSequence(listOf("$BASE/wrong_reveal.mp3", num(answer)), onDone)
    }

    /** 超时：播"时间到了，答案是" + 数字 */
    fun playTimeout(answer: Int, onDone: () -> Unit) {
        player.playSequence(listOf("$BASE/timeout.mp3", num(answer)), onDone)
    }

    fun playFinalResult(correct: Int, total: Int, onDone: () -> Unit = {}) {
        val clip = when {
            correct.toFloat() / total >= 0.8f -> RESULT_GREAT
            correct.toFloat() / total >= 0.5f -> RESULT_OK
            else                               -> RESULT_TRY
        }
        player.playSequence(listOf(clip), onDone)
    }

    fun stop() = player.stop()
    fun release() = player.release()

    private fun num(n: Int) = "$BASE/num_$n.mp3"
    private fun op(op: Operator) = when (op) {
        Operator.ADD      -> "$BASE/op_add.mp3"
        Operator.SUBTRACT -> "$BASE/op_sub.mp3"
    }
}
