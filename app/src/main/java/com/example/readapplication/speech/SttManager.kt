package com.example.readapplication.speech

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File

class SttManager(private val context: Context) {

    enum class Status { READY, MODEL_MISSING, ERROR }

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val MODEL_DIR = "vosk-model-small-cn"
        private val NUMBER_GRAMMAR =
            """["零","一","二","三","四","五","六","七","八","九","十","十一","十二","十三","十四","十五","十六","十七","十八","十九","二十","两","[unk]"]"""
    }

    private var model: Model? = null
    private var recognizer: Recognizer? = null

    @Volatile private var isListening = false
    @Volatile private var sessionId = 0
    private var currentAr: AudioRecord? = null

    suspend fun initialize(): Status = withContext(Dispatchers.IO) {
        val modelPath = resolveModelPath() ?: return@withContext Status.MODEL_MISSING
        return@withContext try {
            model = Model(modelPath)
            recognizer = Recognizer(model, SAMPLE_RATE.toFloat(), NUMBER_GRAMMAR)
            Status.READY
        } catch (e: Exception) {
            Status.ERROR
        }
    }

    @SuppressLint("MissingPermission")
    fun startListening(
        scope: CoroutineScope,
        onPartial: ((String) -> Unit)? = null,
        onResult: (String) -> Unit
    ) {
        if (isListening || recognizer == null) return
        isListening = true
        val mySession = ++sessionId

        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        val ar = AudioRecord(
            MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize * 4
        )
        if (ar.state != AudioRecord.STATE_INITIALIZED) {
            ar.release()
            isListening = false
            return
        }
        currentAr = ar
        ar.startRecording()

        scope.launch(Dispatchers.IO) {
            val buffer = ShortArray(bufferSize)
            while (isListening) {
                val nRead = ar.read(buffer, 0, buffer.size)
                if (nRead < 0) break   // AR 被 stop()，退出循环
                if (nRead == 0) continue

                if (recognizer?.acceptWaveForm(buffer, nRead) == true) {
                    val text = recognizer?.getResult()?.extractText("text") ?: ""
                    if (text.isNotEmpty()) {
                        withContext(Dispatchers.Main) { onResult(text) }
                        break
                    }
                } else {
                    val partial = recognizer?.getPartialResult()?.extractText("partial") ?: ""
                    if (partial.isNotEmpty()) {
                        withContext(Dispatchers.Main) { onPartial?.invoke(partial) }
                    }
                }
            }
            // 用本地 ar 清理，绝不触碰 currentAr（可能已属于新会话）
            ar.stop()
            ar.release()
            if (currentAr === ar) currentAr = null
            // 只有当前会话才更新共享状态，防止旧会话覆盖新会话
            if (sessionId == mySession) {
                isListening = false
                recognizer?.reset()
            }
        }
    }

    fun stopListening() {
        isListening = false
        val ar = currentAr
        currentAr = null
        ar?.stop()  // 让 IO 协程的 ar.read() 立即返回错误值，解除阻塞
        // release() 由 IO 协程负责，这里只 stop
    }

    fun release() {
        stopListening()
        recognizer?.close()
        model?.close()
        recognizer = null
        model = null
    }

    private fun resolveModelPath(): String? {
        val filesDir = File(context.filesDir, MODEL_DIR)
        if (filesDir.exists() && filesDir.isDirectory) return filesDir.absolutePath
        if (hasModelInAssets()) {
            copyFromAssets(MODEL_DIR, filesDir)
            return filesDir.absolutePath
        }
        return null
    }

    private fun hasModelInAssets(): Boolean = try {
        context.assets.list(MODEL_DIR)?.isNotEmpty() == true
    } catch (e: Exception) { false }

    private fun copyFromAssets(assetPath: String, destDir: File) {
        destDir.mkdirs()
        context.assets.list(assetPath)?.forEach { name ->
            val srcPath = "$assetPath/$name"
            val destFile = File(destDir, name)
            val children = try { context.assets.list(srcPath) } catch (e: Exception) { null }
            if (!children.isNullOrEmpty()) {
                copyFromAssets(srcPath, destFile)
            } else {
                context.assets.open(srcPath).use { it.copyTo(destFile.outputStream()) }
            }
        }
    }

    private fun String.extractText(key: String): String = try {
        JSONObject(this).optString(key, "")
    } catch (e: Exception) { "" }
}
