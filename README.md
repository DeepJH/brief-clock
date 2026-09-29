# Brief Clock (讲解时)

Brief Clock（中文名：**讲解时**）是一个现代、简洁且富有特色的 Android 智能语音时钟与小憩自律轮盘赌应用。

## 特色功能 (Features)

1. **Brief Alert (录音设闹钟)**
   - **双层物理翻页时钟 (Solari Split-Flap Clock)**：顶部采用经典双层分体机械翻页时钟，上下半页在中间铰链缝隙精准裁切配合 3D 轴测翻折，数字翻折时上页自然下翻、下页平滑就位，带有明暗折光与铰链铆钉机械细节。
   - **底部胶囊录音按钮**：底部正中宽幅胶囊形态按钮，长按实时录音伴随呼吸发光动效，松手即刻唤起快捷设闹钟弹窗，录音直达铃声。
   - **双轨唤醒调度**：同步调用 Android 原生系统闹钟（`AlarmClock.ACTION_SET_ALARM` + `FileProvider` 音频直传），配合本地前台服务（`AlarmAlertService` + `AlarmAlertActivity`），锁屏穿透准时唤醒。
   - **常规闹钟管理**：顶栏快捷 `+` 手动添加、列表展示、开关切换、滑动删除与语音试听。

2. **Nap Roulette (小憩轮盘赌)**
   - **上下滚动滚轮选时**：阻尼滚轮上下滑动选择目标小憩时长（支持 10s 测试模式及常规时长），配备居中透镜与触感震动反馈。
   - **3D 轴测无光影悬浮左轮**：纯原生 Compose Canvas 绘制 3D 轴测切面左轮手枪，无多余光影直接绘于屏幕背景；具备 3D 转轮旋转、弹巢膛孔轨道转动、击锤后扳、扳机扣动、后坐力仰角与枪口爆燃火光。
   - **小憩胜负博弈规则**：
     - **未睡够目标时长提前醒来**：扣动扳机判负！触发枪口烈焰、后坐力仰角、马格南震颤枪声与振动惩罚；
     - **睡够目标时长醒来**：扣动扳机判胜！触发空仓“咔哒”清脆金属撞针击发音效。
   - **规则说明弹窗**：独立规则说明图标按钮，弹窗查看完整玩法，界面干练无多余干扰。
   - **物理声学音频引擎**：物理建模多频段 DSP 算法合成（超音速初段破空 + 850Hz 爆燃体 + 160Hz→38Hz 低音下潜 + 室内混响残响 + 双曲正切饱和防破音），配合静态 PCM 内存预加载，零延迟发声。
   - **Emoji 矢量图标**：底部导航栏与界面全面套用开源无版权 Twemoji 经典左轮手枪矢量图（`ic_emoji_revolver.xml`）。

3. **Statistics (数据统计)**
   - **存活率环形图 (Donut Chart)**：直观展示胜负比例与百分比胜率。
   - **近 7 天小憩趋势柱状图 (Weekly Bar Chart)**：动态呈现每日小憩时长分布与违规警示。
   - **指标卡片与战绩明细**：总对局、存活数、中弹数、总时长、平均时长、当前连胜及历史最高连胜记录。
   - **历史对局回放与清空**：查看近期每次小憩目标与实际时长，支持安全清空历史数据。

4. **个性化偏好 (Preferences & Theming)**
   - **5 种主题主色调**：默认经典电光蓝（Blue），可选深红（Crimson）、翠绿（Emerald）、幻紫（Violet）、琥珀橙（Amber）。
   - **明暗外观**：支持跟随系统（System）、浅色模式（Light）、深色模式（Dark）。
   - **多语言即时热切换**：支持跟随系统、English、简体中文（应用中文名：讲解时）即时无缝切换，完全保证 Activity 上下文安全防崩溃。

## 技术栈与工程架构

- **UI 框架**：Jetpack Compose + Material Design 3
- **开发语言**：Kotlin 2.0.20
- **构建系统**：Gradle 9.5.1 + AGP 8.5.2
- **最低兼容**：`minSdkVersion 26` (Android 8.0)
- **目标平台**：`targetSdkVersion 35` (Android 15)
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
