package com.example.readapplication.model

enum class Difficulty(
    val label: String,
    val emoji: String,
    val description: String,
    val maxNumber: Int,
    val allowSubtraction: Boolean
) {
    EASY("简单", "🌱", "10以内加法", 10, false),
    MEDIUM("中等", "🌿", "10以内加减法", 10, true),
    HARD("困难", "🌳", "20以内加减法", 20, true)
}
