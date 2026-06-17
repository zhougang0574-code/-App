package com.example.readapplication.speech

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

class AudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun playSequence(assetPaths: List<String>, onDone: () -> Unit) {
        playNext(assetPaths, 0, onDone)
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun release() = stop()

    private fun playNext(paths: List<String>, index: Int, onDone: () -> Unit) {
        if (index >= paths.size) {
            onDone()
            return
        }
        playSingle(paths[index]) { playNext(paths, index + 1, onDone) }
    }

    private fun playSingle(assetPath: String, onComplete: () -> Unit) {
        try {
            mediaPlayer?.release()
            val afd = context.assets.openFd(assetPath)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                setOnCompletionListener { mainHandler.post { onComplete() } }
                setOnErrorListener { _, _, _ -> mainHandler.post { onComplete() }; true }
                prepare()
                start()
            }
        } catch (e: Exception) {
            mainHandler.post { onComplete() }  // 文件缺失时跳过，不阻断流程
        }
    }
}
