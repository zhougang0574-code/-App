package com.example.readapplication.viewmodel

enum class QuizPhase {
    IDLE,       // 未开始
    SPEAKING,   // TTS 正在播报题目
    LISTENING,  // 等待语音输入
    FEEDBACK,   // 显示答题反馈
    FINISHED    // 10题全部完成
}
