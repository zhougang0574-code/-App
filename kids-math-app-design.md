# 幼儿算术学习 App 设计文档

## 一、项目概述

**产品名称**：小算星（暂定）  
**目标用户**：3-7岁幼儿（家长陪同或独立使用）  
**核心功能**：语音交互式加减法练习，自动出题 + 语音播报 + 语音答题  
**运行环境**：Android（完全离线，无需联网）

---

## 二、功能列表

### 2.1 主要功能

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 难度选择 | 三档难度，进入题目前选择 | P0 |
| 自动出题 | 每轮10道题，随机生成 | P0 |
| TTS 语音播报 | 播报题目，如"3 加 5 等于多少？" | P0 |
| 语音识别答题 | 离线识别小朋友语音，提取数字 | P0 |
| 答案校验 | 判断对错，给出即时反馈 | P0 |
| 本轮总结页 | 10题结束后显示得分、答对数量 | P1 |
| 动画奖励 | 答对时播放星星/动物庆祝动画 | P1 |
| 倒计时 | 每题有思考时间倒计时（可配置） | P1 |
| 音调过滤 | 过滤成人声音，只响应儿童音调 | P2 |

### 2.2 难度分级

| 等级 | 图标 | 出题范围 | 说明 |
|------|------|----------|------|
| 简单 🌱 | 小星星 | 10以内加法 | 只有加法，结果 ≤ 10 |
| 中等 🌿 | 中星星 | 10以内加减法 | 加减混合，不借位 |
| 困难 🌳 | 大星星 | 20以内加减法 | 加减混合，可借位 |

---

## 三、页面流程

```
启动页（Logo + 进入按钮）
    ↓
难度选择页
    ↓
答题页（核心页面）
    ├── 显示题号（第X题/共10题）
    ├── 显示题目（大字：3 + 5 = ？）
    ├── TTS 自动播报题目
    ├── 倒计时圆圈动画
    ├── 语音按钮（按住说话 或 自动监听）
    ├── 答对 → 庆祝动画 → 下一题
    └── 答错 → 提示重答（最多2次）→ 公布答案 → 下一题
    ↓
结果总结页
    ├── 得分展示（星星数量）
    ├── 答对X题 / 共10题
    ├── 每题回顾列表
    └── 再来一次 / 换难度 按钮
```

---

## 四、技术选型

### 4.1 开发语言 & 框架

| 技术 | 选型 | 理由 |
|------|------|------|
| 语言 | Kotlin | Android 官方推荐 |
| UI 框架 | Jetpack Compose | 现代声明式 UI，适合动画 |
| 动画 | Lottie | 轻量，支持 After Effects 动画，有丰富免费儿童素材 |

### 4.2 语音相关

| 功能 | 技术 | 说明 |
|------|------|------|
| 语音播报 (TTS) | Android TextToSpeech | 内置，无需第三方，完全离线 |
| 语音识别 (STT) | Vosk Android | 离线，支持语法约束，只识别数字词汇 |
| 语音模型 | vosk-model-small-cn | 中文小模型，约50MB，打包进APK |

### 4.3 核心依赖

```gradle
dependencies {
    // 语音识别
    implementation 'com.alphacephei:vosk-android:0.3.47'
    
    // 动画
    implementation 'com.airbnb.android:lottie-compose:6.1.0'
    
    // Jetpack Compose
    implementation platform('androidx.compose:compose-bom:2024.02.00')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.material3:material3'
    
    // 音频处理（P2 音调过滤用）
    implementation 'be.tarsos.dsp:core:2.5'
}
```

---

## 五、模块设计

### 5.1 模块划分

```
app/
├── ui/
│   ├── home/          # 启动页
│   ├── difficulty/    # 难度选择页
│   ├── quiz/          # 答题页（核心）
│   └── result/        # 结果总结页
├── engine/
│   ├── QuestionGenerator.kt   # 出题逻辑
│   ├── AnswerValidator.kt     # 答案校验
│   └── NumberParser.kt        # 语音文字 → 数字
├── speech/
│   ├── TtsManager.kt          # 语音播报管理
│   ├── SttManager.kt          # 语音识别管理
│   └── AudioFilter.kt         # 音调过滤（P2）
└── model/
    ├── Question.kt            # 题目数据模型
    ├── QuizSession.kt         # 本轮会话状态
    └── Difficulty.kt          # 难度枚举
```

### 5.2 核心数据模型

```kotlin
data class Question(
    val num1: Int,
    val num2: Int,
    val operator: Operator,  // ADD / SUBTRACT
    val answer: Int
)

data class QuizSession(
    val difficulty: Difficulty,
    val questions: List<Question>,  // 10题
    val answers: List<Int?>,        // 用户答案
    val currentIndex: Int = 0
)

enum class Difficulty(val label: String, val max: Int) {
    EASY("简单", 10),
    MEDIUM("中等", 10),
    HARD("困难", 20)
}
```

### 5.3 出题逻辑

```kotlin
object QuestionGenerator {
    fun generate(difficulty: Difficulty, count: Int = 10): List<Question> {
        return (1..count).map {
            when (difficulty) {
                Difficulty.EASY -> generateAddition(max = 10)
                Difficulty.MEDIUM -> generateMixed(max = 10)
                Difficulty.HARD -> generateMixed(max = 20)
            }
        }
    }
    
    private fun generateAddition(max: Int): Question {
        val num1 = (1..max).random()
        val num2 = (1..(max - num1)).random()
        return Question(num1, num2, Operator.ADD, num1 + num2)
    }
}
```

### 5.4 语音识别数字转换

```kotlin
object NumberParser {
    private val chineseMap = mapOf(
        "零" to 0, "一" to 1, "二" to 2, "两" to 2,
        "三" to 3, "四" to 4, "五" to 5, "六" to 6,
        "七" to 7, "八" to 8, "九" to 9, "十" to 10,
        "十一" to 11, "十二" to 12, "十三" to 13,
        "十四" to 14, "十五" to 15, "十六" to 16,
        "十七" to 17, "十八" to 18, "十九" to 19, "二十" to 20
    )
    
    fun parse(text: String): Int? = chineseMap[text.trim()]
}
```

---

## 六、UI 设计规范

### 6.1 设计风格

- **主题**：星球探险 / 森林小动物（二选一）
- **配色**：暖色系为主，高饱和度（黄、橙、绿、蓝）
- **字体**：圆体，大字号（答题区不小于 72sp）
- **按钮**：圆角大按钮，有按压反馈动画
- **背景**：渐变天空色 + 云朵/星星装饰

### 6.2 答题页布局

```
┌─────────────────────────────┐
│  第 3 题 / 共 10 题    ⭐⭐⭐  │  ← 题号 + 已得星星
│                             │
│         3  +  5  =  ?       │  ← 大字题目（72sp）
│                             │
│      [倒计时圆圈动画 10s]    │
│                             │
│                             │
│       🎤 按住说出答案        │  ← 语音按钮（大圆形）
│                             │
│     [ 跳过 ]    [ 重播 ]     │  ← 辅助按钮
└─────────────────────────────┘
```

### 6.3 反馈动画

- **答对**：Lottie 星星爆炸 + 小动物跳舞，TTS 播报"太棒了！答对啦！"
- **答错**：轻微摇晃动画，TTS 播报"再想想～"（最多提示2次）
- **超时**：温柔提示，公布答案，TTS 播报"答案是X，我们继续！"

---

## 七、开发阶段规划

### Phase 1 - 核心功能（MVP）
- [ ] 项目结构搭建
- [ ] 出题逻辑 + 校验
- [ ] TTS 语音播报
- [ ] Vosk 语音识别集成
- [ ] 基础答题流程跑通

### Phase 2 - UI 完善
- [ ] Jetpack Compose UI 实现
- [ ] Lottie 动画集成
- [ ] 难度选择页
- [ ] 结果总结页

### Phase 3 - 体验优化
- [ ] 音调过滤（只响应儿童声音）
- [ ] 声音/动画设置
- [ ] 错误题目重练功能

---

## 八、注意事项

1. **麦克风权限**：AndroidManifest 声明 `RECORD_AUDIO`，首次使用时引导授权
2. **Vosk 模型加载**：首次启动从 assets 复制到内部存储，显示加载进度
3. **TTS 与 STT 互斥**：播报题目时不能监听，避免识别到自己的声音
4. **儿童隐私**：不收集任何数据，无网络请求，符合 COPPA
5. **APK 大小**：Vosk 模型约 50MB，注意控制总包体积
