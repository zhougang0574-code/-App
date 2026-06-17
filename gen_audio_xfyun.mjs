import WebSocket from 'ws';
import crypto from 'crypto';
import fs from 'fs';
import path from 'path';

const APP_ID     = '41d2e860';
const API_KEY    = '145c730bfb41828dbb54ad995c65519e';
const API_SECRET = 'ZDI0ODBjYTBlYzcxMjU3MDE2NmY5NTNh';

const VOICE_NEUTRAL = 'x4_mengmengneutral';  // 萌萌·中立
const VOICE_HAPPY   = 'x_mengmenghappy';      // 萌萌·高兴，仅用于答对反馈
const OUTPUT_DIR    = 'app/src/main/assets/audio';

function buildWsUrl() {
  const date = new Date().toUTCString();
  const host = 'tts-api.xfyun.cn';
  const reqPath = '/v2/tts';
  const sigSrc = `host: ${host}\ndate: ${date}\nGET ${reqPath} HTTP/1.1`;
  const sig = crypto.createHmac('sha256', API_SECRET).update(sigSrc).digest('base64');
  const authSrc = `api_key="${API_KEY}", algorithm="hmac-sha256", headers="host date request-line", signature="${sig}"`;
  const auth = Buffer.from(authSrc).toString('base64');
  return `wss://${host}${reqPath}?authorization=${encodeURIComponent(auth)}&date=${encodeURIComponent(date)}&host=${host}`;
}

function synthesize(text, filename, voice) {
  return new Promise((resolve, reject) => {
    const outPath = path.join(OUTPUT_DIR, filename);
    const ws = new WebSocket(buildWsUrl());
    const chunks = [];
    const timer = setTimeout(() => { ws.terminate(); reject(new Error(`超时: ${filename}`)); }, 12000);

    ws.on('open', () => {
      ws.send(JSON.stringify({
        common:   { app_id: APP_ID },
        business: {
          aue: 'lame', sfl: 1,
          auf: 'audio/L16;rate=16000',
          vcn: voice,
          speed: 45,
          volume: 80,
          pitch: 55,
          tte: 'UTF8'
        },
        data: { status: 2, text: Buffer.from(text).toString('base64') }
      }));
    });

    ws.on('message', raw => {
      const msg = JSON.parse(raw.toString());
      if (msg.code !== 0) {
        clearTimeout(timer); ws.close();
        reject(new Error(`讯飞错误 ${msg.code}: ${msg.message}`));
        return;
      }
      if (msg.data?.audio) chunks.push(Buffer.from(msg.data.audio, 'base64'));
      if (msg.data?.status === 2) {
        clearTimeout(timer); ws.close();
        fs.writeFileSync(outPath, Buffer.concat(chunks));
        console.log(`  ✓  ${filename.padEnd(22)} ← "${text}"`);
        resolve();
      }
    });

    ws.on('error', e => { clearTimeout(timer); reject(e); });
  });
}

// [文本, 文件名, 音色]
const TASKS = [
  // ── 数字 0-20（中立）─────────────────────────────────────────
  ...['零','一','二','三','四','五','六','七','八','九','十',
      '十一','十二','十三','十四','十五','十六','十七','十八','十九','二十']
    .map((w, i) => [w, `num_${i}.mp3`, VOICE_NEUTRAL]),

  // ── 运算符（中立）────────────────────────────────────────────
  ['加',   'op_add.mp3',  VOICE_NEUTRAL],
  ['减',   'op_sub.mp3',  VOICE_NEUTRAL],

  // ── 题目后缀（中立）──────────────────────────────────────────
  ['等于多少', 'q_suffix.mp3', VOICE_NEUTRAL],

  // ── 答对反馈（高兴）──────────────────────────────────────────
  ['太棒了，答对啦',     'correct_1.mp3', VOICE_HAPPY],
  ['哇，你真厉害',       'correct_2.mp3', VOICE_HAPPY],
  ['完全正确，继续加油', 'correct_3.mp3', VOICE_HAPPY],

  // ── 重试提示（中立）──────────────────────────────────────────
  ['再想想，重新说一次吧', 'retry.mp3', VOICE_NEUTRAL],

  // ── 答错公布答案（中立）──────────────────────────────────────
  ['答错啦，答案是', 'wrong_reveal.mp3', VOICE_NEUTRAL],

  // ── 超时公布答案（中立）──────────────────────────────────────
  ['时间到了，答案是', 'timeout.mp3', VOICE_NEUTRAL],

  // ── 本轮结束（高兴/中立）─────────────────────────────────────
  ['本轮结束，你太厉害了',         'result_great.mp3', VOICE_HAPPY],
  ['本轮结束，继续加油，越来越棒', 'result_ok.mp3',    VOICE_NEUTRAL],
  ['本轮结束，多多练习，下次一定更好', 'result_try.mp3', VOICE_NEUTRAL],
];

fs.mkdirSync(OUTPUT_DIR, { recursive: true });
console.log(`\n开始生成 ${TASKS.length} 个音频...\n`);

for (const [text, file, voice] of TASKS) {
  try {
    await synthesize(text, file, voice);
    await new Promise(r => setTimeout(r, 200));
  } catch (err) {
    console.error(`  ✗  ${file}: ${err.message}`);
  }
}

console.log('\n全部完成！');
