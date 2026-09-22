# Brief Clock (简钟)

Brief Clock 是一个现代、简洁且具有特色的 Android 时钟与小憩轮盘赌应用（MVP）。

## 特色功能 (Features)

1. **Brief Alarm (录音闹钟)**
   - **常规闹钟管理**：支持闹钟开关、列表展示、标签设定、精确唤醒调度（`AlarmManager.setAlarmClock`）。
   - **按住说话录音**：长按主面板麦克风按钮实时录音，松手后一键唤起设置弹窗，将专属语音设为唤醒铃声。
   - **语音试听与重录**：弹窗内支持无限制时长试听与快速重录。
   - **双轨唤醒调度**：同步调用 Android 系统原生闹钟接口（`AlarmClock.ACTION_SET_ALARM` + `FileProvider` 音频流），并配合本地前台全屏唤醒服务（`AlarmAlertService` + `AlarmAlertActivity`），确保各品牌 ROM 下均能准时响铃。

2. **Nap Roulette (小憩轮盘赌)**
   - **2.5D 左轮手枪 Canvas 动画**：纯原生 Compose Canvas 绘制 2.5D 金属质感枪身、6 孔转轮弹巢、击锤、扳机及握把，具备转轮旋转、击锤张开、扳机扣动、后坐力位移与枪口爆燃火光特效。
   - **小憩胜负博弈规则**：设定目标小憩时间（10s测试模式、15m、20m、30m、45m）并开赌；
     - **未睡够目标时间提前醒来**：扣动扳机判负！触发爆燃枪口火花、后坐力位移、震撼枪声音效与震动；
     - **睡够目标时间醒来**：扣动扳机判胜！触发空仓“咔哒”撞针击发音效与存活通关。
   - **内置高保真无版权音频合成引擎**：基于 `AudioTrack` 纯程序化 PCM 实时物理合成枪声、空仓撞击、转轮转动与击锤张开音效，零外部资源依赖，零延迟响应。

3. **Statistics (数据统计)**
   - **存活率环形图 (Donut Chart)**：直观展示胜负比例与百分比胜率。
   - **近 7 天小憩趋势柱状图 (Weekly Bar Chart)**：动态呈现每日小憩时长分布与违规警示。
   - **指标卡片与战绩明细**：总对局、存活数、违规中弹数、总时长、平均时长、当前连胜及历史最高连胜记录。
   - **历史对局回放与清空**：查看近期每次小憩目标与实际时长，支持安全清空历史数据。

## 技术栈与工程架构

- **UI 框架**：Jetpack Compose + Material Design 3
- **开发语言**：Kotlin 2.0.20
- **构建系统**：Gradle 9.5.1 + AGP 8.5.2
- **最低兼容**：`minSdkVersion 26` (Android 8.0)
- **目标平台**：`targetSdkVersion 35` (Android 15)
- **多语言**：默认英文，完整支持简体中文 (`values-zh-rCN`)
- **数据存储**：轻量原生 SQLite 数据库 (`BriefClockDatabase`)，零额外 KSP/APT 依赖，极速编译
- **开源协议**：GNU General Public License v3.0 (GPL-3.0)

## 构建与测试

```bash
# 运行单元测试
./gradlew testDebugUnitTest

# 编译 Debug APK
./gradlew assembleDebug

# 编译 Release APK
./gradlew assembleRelease
```
