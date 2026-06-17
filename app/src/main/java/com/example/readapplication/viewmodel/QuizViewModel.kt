package com.example.readapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.readapplication.engine.AnswerValidator
import com.example.readapplication.engine.NumberParser
import com.example.readapplication.engine.QuestionGenerator
import com.example.readapplication.model.AnswerResult
import com.example.readapplication.model.Difficulty
import com.example.readapplication.model.Question
import com.example.readapplication.speech.SpeechPlayer
import com.example.readapplication.speech.SttManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val speechPlayer = SpeechPlayer(application)
    private val sttManager = SttManager(application)

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val sttStatus = sttManager.initialize()
            _uiState.update { it.copy(isInitializing = false, sttStatus = sttStatus) }
        }
    }

    fun startQuiz(difficulty: Difficulty) {
        timerJob?.cancel()
        sttManager.stopListening()
        speechPlayer.stop()
        val questions = QuestionGenerator.generate(difficulty)
        _uiState.update {
            it.copy(
                difficulty = difficulty,
                questions = questions,
                currentIndex = 0,
                retryCount = 0,
                answers = emptyList(),
                phase = QuizPhase.SPEAKING,
                recognizedText = ""
            )
        }
        speakCurrentQuestion()
    }

    fun onEvent(event: QuizEvent) {
        when (event) {
            is QuizEvent.RepeatQuestion -> {
                timerJob?.cancel()
                sttManager.stopListening()
                speechPlayer.stop()
                speakCurrentQuestion()
            }
            is QuizEvent.SkipQuestion -> {
                timerJob?.cancel()
                sttManager.stopListening()
                handleTimeout()
            }
        }
    }

    fun resetSession() {
        timerJob?.cancel()
        sttManager.stopListening()
        speechPlayer.stop()
        _uiState.update { QuizUiState(isInitializing = false, sttStatus = it.sttStatus) }
    }

    private fun speakCurrentQuestion() {
        val question = _uiState.value.currentQuestion ?: return
        _uiState.update { it.copy(phase = QuizPhase.SPEAKING, recognizedText = "") }
        speechPlayer.playQuestion(question) { startListening() }
    }

    private fun startListening() {
        _uiState.update { it.copy(phase = QuizPhase.LISTENING, timeLeft = QuizUiState.QUESTION_TIMEOUT) }
        startCountdown()
        sttManager.startListening(
            scope = viewModelScope,
            onPartial = { partial ->
                _uiState.update { it.copy(recognizedText = partial) }
            },
            onResult = { result ->
                timerJob?.cancel()
                sttManager.stopListening()
                processAnswer(result)
            }
        )
    }

    private fun processAnswer(text: String) {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        val userAnswer = NumberParser.parse(text)
        val isCorrect = userAnswer != null && AnswerValidator.check(question, userAnswer)

        _uiState.update { it.copy(phase = QuizPhase.FEEDBACK, recognizedText = text) }

        when {
            isCorrect -> {
                _uiState.update { it.copy(answers = it.answers + AnswerResult(question, userAnswer, true), retryCount = 0) }
                speechPlayer.playCorrect { advanceQuestion() }
            }
            state.retryCount < QuizUiState.MAX_RETRIES -> {
                _uiState.update { it.copy(retryCount = it.retryCount + 1) }
                speechPlayer.playRetry { startListening() }
            }
            else -> {
                _uiState.update { it.copy(answers = it.answers + AnswerResult(question, userAnswer, false), retryCount = 0) }
                speechPlayer.playWrongAnswer(question.answer) { advanceQuestion() }
            }
        }
    }

    private fun handleTimeout() {
        val question = _uiState.value.currentQuestion ?: return
        val result = AnswerResult(question, null, false, isTimeout = true)
        _uiState.update { it.copy(answers = it.answers + result, retryCount = 0) }
        speechPlayer.playTimeout(question.answer) { advanceQuestion() }
    }

    private fun advanceQuestion() {
        val nextIndex = _uiState.value.currentIndex + 1
        if (nextIndex >= _uiState.value.totalQuestions) {
            _uiState.update { it.copy(phase = QuizPhase.FINISHED, currentIndex = nextIndex) }
            speakFinalResult()
        } else {
            _uiState.update { it.copy(currentIndex = nextIndex) }
            speakCurrentQuestion()
        }
    }

    private fun speakFinalResult() {
        val state = _uiState.value
        speechPlayer.playFinalResult(state.correctCount, state.totalQuestions)
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (i in QuizUiState.QUESTION_TIMEOUT downTo 0) {
                _uiState.update { it.copy(timeLeft = i) }
                if (i == 0) {
                    sttManager.stopListening()
                    handleTimeout()
                    return@launch
                }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        speechPlayer.release()
        sttManager.release()
    }
}
