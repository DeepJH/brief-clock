# Brief Clock (讲解时) - Release v0.1

## 版本概述 (Release Overview)

Brief Clock（中文名：**讲解时**）正式发布 **RELEASE 0.1**！该版本集成了语音快设闹钟、Solari 物理分体机械翻页时钟、无光影 3D 轴测左轮手枪与纯粹盲盒自律小憩博弈，并支持高保真物理声学引擎、Material Design 3 多主题色与全中文本地化。

---

## 核心功能与亮点 (Key Features)

### 1. Nap Roulette 盲盒小憩轮盘赌
- **真·盲盒博弈**：小憩计时期间绝不显示已睡时长或倒计时进度（界面显示 `? ? : ? ?` 盲盒提示），杜绝时间剧透，全凭人体生物钟直觉；
- **博弈胜负裁决**：
  - 睡够目标时长点击醒来：清脆撞针“咔哒”击发，成功存活！
  - 提前违规醒来：马格南“砰！”震颤枪响与枪口火光，赌局失败！
- **触控解压音效**：直接点击左轮左半侧枪管开火试听，点击右半侧转轮旋转弹仓。
- **3D 轴测无光影左轮**：原生 Compose 纯多边形切面绘制，无刺眼渐变与伪光影，直接悬浮在界面背景。

### 2. Brief Alert 语音快设与机械翻页钟
- **Solari 物理双层机械翻页钟**：真实模拟上/下半页独立分体翻折，上半页前翻下坠、下半页平滑就位，配合铰链缝隙与金属铆钉细节。
- **底部胶囊录音按钮**：底部正中宽幅胶囊形态按钮，长按录制唤醒语音，松手秒设专属闹钟。
- **双轨系统唤醒**：系统原生 `AlarmClock` 接口 + 本地前台锁屏穿透服务，确保各品牌 Android 系统准时响铃。

### 3. 物理声学引擎 (Procedural DSP Audio)
- 基于 `AudioTrack` 纯程序化多频段物理建模（超音速破空 + 850Hz 爆燃体 + 160Hz→38Hz 低音下潜 + 室内混响），零外部文件依赖，静态 PCM 预加载 0 延迟即刻发声。

### 4. 个性化与稳定性
- **主题主色调**：默认经典电光蓝（Blue），可选深红、翠绿、幻紫、琥珀橙 5 款主题。
- **外观与语言**：支持跟随系统、浅色、深色模式；支持跟随系统、English、简体中文（讲解时）秒级防崩溃热切换。

---

## 产物校验 (Artifacts)

| 文件名 | 文件类型 | 大小 | SHA256 校验和 |
|---|---|---|---|
| `app/build/outputs/apk/debug/app-debug.apk` | Debug APK | ~15MB | `09d53524fc8df1a278a1078e1ef2be969f537a7df646e2c9c93ee9e539b4ebae` |
| `app/build/outputs/apk/release/app-release-unsigned.apk` | Release APK | ~6.4MB | `efc0f283a52c0519e57e2859dab36fb98b35bcff5f529a91a1498aa0b102b5e7` |

---

## 安装与快速启动 (Installation)

```bash
# 1. 编译并安装到已连接设备或模拟器
./gradlew installDebug

# 2. 启动应用
adb shell am start -n com.briefclock.app.debug/com.briefclock.app.MainActivity

# 或通过 adb 直接安装生成的 APK
adb install -r -t app/build/outputs/apk/debug/app-debug.apk
```
