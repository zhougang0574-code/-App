package com.example.readapplication.speech

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class TtsManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var pendingOnDone: (() -> Unit)? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    suspend fun initialize(): Boolean = suspendCancellableCoroutine { cont ->
        tts = TextToSpeech(context) { status ->
            if (!cont.isActive) return@TextToSpeech
            if (status == TextToSpeech.SUCCESS) {
                setupEngine()
                cont.resume(true)
            } else {
                cont.resume(false)
            }
        }
    }

    private fun setupEngine() {
        val engine = tts ?: return

        // 优先通过 voices API 找离线中文语音（更可靠）
        val chineseVoice = engine.voices?.firstOrNull { v ->
            v.locale.language == "zh" && !v.isNetworkConnectionRequired
        }
        if (chineseVoice != null) {
            engine.voice = chineseVoice
        } else {
            // 依次尝试多种中文 Locale，找到支持的为止
            val candidates = listOf(
                Locale("zh", "CN"),
                Locale.CHINA,
                Locale.CHINESE,
                Locale("zh"),
                Locale.getDefault()
            )
            for (locale in candidates) {
                val r = engine.setLanguage(locale)
                if (r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED) break
            }
        }

        engine.setSpeechRate(0.85f)

        // 明确走媒体音频通道，避免被系统静音
        engine.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )

        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) = mainHandler.post { fireDone() }.let {}
            override fun onError(id: String?) = mainHandler.post { fireDone() }.let {}
        })
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        pendingOnDone = onDone
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }
        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "utt_${System.currentTimeMillis()}")
        if (result == null || result == TextToSpeech.ERROR) {
            mainHandler.post { fireDone() }
        }
    }

    fun stop() {
        tts?.stop()
        pendingOnDone = null
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    private fun fireDone() {
        val cb = pendingOnDone
        pendingOnDone = null
        cb?.invoke()
    }
}
