#!/usr/bin/env python3
"""
生成小算星 App 所需的中文语音文件

安装依赖：
  pip install gTTS

运行：
  python generate_audio.py

完成后将生成的 audio/ 文件夹整体复制到：
  app/src/main/assets/audio/
"""

import os
import time

try:
    from gtts import gTTS
except ImportError:
    print("请先安装 gTTS：pip install gTTS")
    exit(1)

OUTPUT_DIR = "audio"
LANG = "zh-cn"

# 数字 0-20
NUMBERS = {
    0:  "零",   1:  "一",   2:  "二",   3:  "三",   4:  "四",
    5:  "五",   6:  "六",   7:  "七",   8:  "八",   9:  "九",
    10: "十",   11: "十一", 12: "十二", 13: "十三", 14: "十四",
    15: "十五", 16: "十六", 17: "十七", 18: "十八", 19: "十九",
    20: "二十"
}

# 其余语音片段
CLIPS = {
    "op_add":       "加",
    "op_sub":       "减",
    "q_suffix":     "等于多少",
    "correct_1":    "太棒了，答对啦",
    "correct_2":    "真厉害，你真聪明",
    "correct_3":    "哇，答对了，继续加油",
    "retry":        "再想想，重新说一次吧",
    "timeout":      "时间到了，答案是",
    "result_great": "本轮结束，你太厉害了，加油继续",
    "result_ok":    "本轮结束，继续加油，你会越来越棒的",
    "result_try":   "本轮结束，多多练习，下次一定会更好",
}


def generate(filename: str, text: str):
    path = os.path.join(OUTPUT_DIR, filename)
    print(f"  生成: {filename:<20}  \"{text}\"")
    gTTS(text=text, lang=LANG, slow=False).save(path)
    time.sleep(0.3)   # 避免请求过快被限流


def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    total = len(NUMBERS) + len(CLIPS)

    print(f"开始生成 {total} 个音频文件...\n")

    print("【数字】")
    for n, word in NUMBERS.items():
        generate(f"num_{n}.mp3", word)

    print("\n【词语】")
    for name, text in CLIPS.items():
        generate(f"{name}.mp3", text)

    print(f"\n✅ 完成！共生成 {total} 个文件，保存在 {os.path.abspath(OUTPUT_DIR)}/")
    print("\n下一步：")
    print(f"  将 {OUTPUT_DIR}/ 文件夹复制到项目的 app/src/main/assets/audio/")


if __name__ == "__main__":
    main()
