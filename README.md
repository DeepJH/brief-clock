# Brief Clock (讲解时)

Brief Clock (讲解时) 是一个现代、简洁且富有特色的 Android 智能语音时钟与小憩自律轮盘赌应用。

## 免责声明 (Disclaimer)

> **特别声明（无政治立场）**：  
> 本应用中文名称“讲解时 (Brief Clock)”及相关调侃梗纯属同音谐音趣味调侃，旨在为作息习惯与小憩自律增添乐趣，与任何历史或现实政治人物无关。开发者及本应用不代表、亦不持任何政治立场，特此声明，请勿进行任何政治联想或过度解读。  
> 
> **Disclaimer (Non-Political)**:  
> The app name \'讲解时\' (Brief Clock) and associated humor are purely linguistic puns for entertainment and habit building, with no connection to any historical or political figures. The developers and this application hold no political stance.

---

## 特色功能 (Features)

1. **简述闹钟 · 录音直达铃声**
   - **单体机械翻页时钟 (Solari Split-Flap Clock)**：顶部采用经典分体机械翻页时钟，单个数字在中间铰链缝隙精准切分，翻页时上半页自然下翻、下页平滑就位，配合明暗折光与铰链铆钉机械细节。
   - **底部胶囊录音按钮**：底部正中宽幅胶囊形态按钮，长按实时录音伴随呼吸发光动效，松手即刻唤起快捷设闹钟弹窗，录音直达铃声。
   - **双轨唤醒调度**：同步调用 Android 原生系统闹钟（`AlarmClock.ACTION_SET_ALARM` + `FileProvider` 音频直传），配合本地前台服务（`AlarmAlertService` + `AlarmAlertActivity`），锁屏穿透准时唤醒。
   - **常规闹钟管理**：顶栏快捷 `+` 手动添加、列表展示、开关切换、滑动删除与语音试听。

2. **小睡轮盘 · 自律博弈**
   - **自律新标语**：“很焦虑？睡不着？那就让睡觉变成赌博般需要重视的正事。”
   - **1 分钟步进选时**：上下滚动滚轮精准调节 1m..60m（含 10s 快速测试），专为高效午睡设计。
   - **2D 极简互动左轮**：基于开源经典左轮矢量（CC-BY 4.0 许可），直接点击左轮左半部（枪管）开火（枪声+后坐力+火光），点击右半部（转轮）转动弹仓（齿轮声+360°旋转）。
   - **盲盒沉浸博弈规则**：
     - **睡眠期间彻底无走字**：睡眠中屏幕绝不显示走字或倒计时；
     - **1 秒心跳告警按钮**：“醒来”按钮居中呈现，每隔 1 秒变红律动一次（心跳警示节奏）；
     - **醒来裁决**：睡够醒来触发“咔哒”空仓撞针通关；提前醒来触发“砰！”马格南枪声与震颤惩罚；扣动扳机后才揭晓目标与实际用时。
   - **击碎困意彩蛋游戏**：10 秒内连续点击左轮 10 次召唤跳动的 `💤`，点击后界面上下撕裂切入迷幻光环，10 秒内点击飞出的 😴/💤/🛌 射击计分，倒计时结束礼花散落并记录最高分，确认后上下合拢复原。
   - **物理声学音频引擎**：纯程序化多频段 DSP 算法实时合成（超音速破空 + 850Hz 爆燃体 + 160Hz→38Hz 低音下潜 + 室内混响），零外部文件依赖，静态 PCM 预加载 0 延迟发声。

3. **数据统计与赞助支持**
   - **粉色爱心赞助 💖**：顶栏一键呼出支持卡片，包含微信支付与支付宝真实收款码。
   - **存活率环形图 (Donut Chart)**：直观展示胜负比例与百分比胜率。
   - **近 7 天小憩趋势柱状图 (Weekly Bar Chart)**：动态呈现每日小憩时长分布与违规警示。
   - **指标卡片与战绩明细**：总对局、存活数、中弹数、总时长、平均时长、当前连胜及历史最高连胜记录。

4. **个性化偏好与设计**
   - **蓝白极简应用图标**：白色背景，中心大号电光蓝感叹号 `!` 结合横向沙漏直线漏斗，外廓构成饱满正六边形。
   - **5 种主题主色调**：默认经典电光蓝（Blue），可选深红、翠绿、幻紫、琥珀橙。
   - **明暗与语言**：支持跟随系统、浅色、深色模式；支持跟随系统、简体中文（讲解时）、English 即时秒级防崩溃热切换。

---

## 技术栈与工程架构

- **应用名称**：Brief Clock (讲解时)
- **最新版本**：v0.1.3 (versionCode: 4)
- **UI 框架**：Jetpack Compose + Material Design 3
- **开发语言**：Kotlin 2.0.20
- **构建系统**：Gradle 9.5.1 + AGP 8.5.2
- **最低兼容**：`minSdkVersion 26` (Android 8.0)
- **目标平台**：`targetSdkVersion 35` (Android 15)
- **数据存储**：轻量原生 SQLite 数据库 (`BriefClockDatabase`)，零额外 KSP/APT 依赖，极速编译
- **开源协议**：GNU General Public License v3.0 (GPL-3.0)
- **资产协议**：Twemoji Classic Revolver (CC-BY 4.0)

## 构建与测试

```bash
# 运行单元测试
./gradlew testDebugUnitTest

# 编译 Debug APK
./gradlew assembleDebug

# 编译 Release APK
./gradlew assembleRelease
```

编译输出产物位于：
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`

## 安装命令 (Installation)

确保已连接 Android 设备或启动 Android 模拟器，并开启 USB 调试：

### 方式 1：使用 Gradle 一键安装并运行

```bash
# 编译并安装 Debug 版本到已连接的设备或模拟器
./gradlew installDebug
```

安装后即可直接在手机桌面点击打开应用，或者使用 adb 命令直接启动：

```bash
adb shell am start -n com.briefclock.app.debug/com.briefclock.app.MainActivity
```

### 方式 2：使用 ADB 直接安装生成的 APK

若已通过 `./gradlew assembleDebug` 完成编译，可使用 `adb install` 命令直接推送安装：

```bash
# 安装 Debug APK（-r 表示覆盖安装保留数据，-t 表示允许安装测试包）
adb install -r -t app/build/outputs/apk/debug/app-debug.apk
```

### 卸载命令

```bash
# 通过 Gradle 卸载 Debug 版本
./gradlew uninstallDebug

# 或直接通过 adb 卸载
adb uninstall com.briefclock.app.debug
```
