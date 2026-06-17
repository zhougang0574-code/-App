package com.example.readapplication.engine

object NumberParser {

    private val chineseToInt = mapOf(
        "零" to 0, "一" to 1, "二" to 2, "两" to 2,
        "三" to 3, "四" to 4, "五" to 5, "六" to 6,
        "七" to 7, "八" to 8, "九" to 9, "十" to 10,
        "十一" to 11, "十二" to 12, "十三" to 13,
        "十四" to 14, "十五" to 15, "十六" to 16,
        "十七" to 17, "十八" to 18, "十九" to 19, "二十" to 20
    )

    fun parse(text: String): Int? {
        val trimmed = text.trim()
        chineseToInt[trimmed]?.let { return it }
        return trimmed.toIntOrNull()
    }
}
