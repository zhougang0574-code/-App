package com.example.readapplication.engine

object NumberParser {

    private val singleDigit = mapOf(
        "零" to 0, "一" to 1, "二" to 2, "两" to 2,
        "三" to 3, "四" to 4, "五" to 5, "六" to 6,
        "七" to 7, "八" to 8, "九" to 9
    )
    private const val TEN_WORD = "十"

    // 完整词形，按长度从长到短排列，子串兜底匹配时优先匹配长词（避免"十六"被错配成"六"）
    private val fullWords = linkedMapOf(
        "二十" to 20, "十九" to 19, "十八" to 18, "十七" to 17, "十六" to 16,
        "十五" to 15, "十四" to 14, "十三" to 13, "十二" to 12, "十一" to 11,
        "十" to 10, "零" to 0, "一" to 1, "二" to 2, "两" to 2,
        "三" to 3, "四" to 4, "五" to 5, "六" to 6, "七" to 7, "八" to 8, "九" to 9
    )

    fun parse(text: String): Int? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null

        // 1) 整段完全匹配（Vosk 把整个数字识别成一个 token 的常见情况）
        fullWords[trimmed]?.let { return it }
        trimmed.toIntOrNull()?.let { return it }

        // 2) 按空格拆分成多个 token 时，先尝试把它们组合成一个完整数字
        //    （应对"十"被拆成"一""十"、"二十"被拆成"二""十"等识别误差）
        val tokens = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() && it != "[unk]" }
        if (tokens.size > 1) {
            parseSequence(tokens)?.let { return it }
        }

        // 3) 单 token 兜底：取第一个能识别出的数字
        for (token in tokens) {
            fullWords[token]?.let { return it }
            token.toIntOrNull()?.let { return it }
        }

        // 4) 兜底：在整句（去空格）中查找最长匹配的中文数字子串，应对"等于六"这类多余文字
        val compact = trimmed.replace(" ", "")
        for ((word, value) in fullWords) {
            if (compact.contains(word)) return value
        }

        // 5) 兜底：提取阿拉伯数字
        Regex("\\d+").find(trimmed)?.value?.toIntOrNull()?.let { return it }

        return null
    }

    /**
     * 把空格分隔的 token 序列组合成数字，支持：
     *   [十]        -> 10        如 "十"
     *   [X, 十]     -> X*10      如 "一 十" -> 10，"二 十" -> 20
     *   [十, Y]     -> 10+Y      如 "十 六" -> 16
     *   [X, 十, Y]  -> X*10+Y
     */
    private fun parseSequence(tokens: List<String>): Int? {
        val parts = tokens.map { tok ->
            when {
                tok == TEN_WORD -> -1
                singleDigit.containsKey(tok) -> singleDigit[tok]!!
                else -> return null
            }
        }
        return when {
            parts.size == 1 && parts[0] == -1 -> 10
            parts.size == 2 && parts[1] == -1 -> parts[0] * 10
            parts.size == 2 && parts[0] == -1 -> 10 + parts[1]
            parts.size == 3 && parts[1] == -1 -> parts[0] * 10 + parts[2]
            else -> null
        }
    }
}
