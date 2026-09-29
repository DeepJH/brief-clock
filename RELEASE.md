# Brief Clock (讲解时) - v0.1.1

## 版本概述 (Release Overview)

Brief Clock (讲解时) 正式发布 **Brief Clock (讲解时)-v0.1.1**！本次版本带来了翻页时钟底层机制彻底重构、纯粹无时间显示的动效盲盒小憩、开源 CC0 1.0 轴测 3D 左轮手枪多边形切面渲染，以及全界面 100% 深度中文汉化。

---

## 核心更新内容 (Changelog v0.1.1)

### 1. 真实单体翻页时钟重构 (Single-Number Split-Flap Clock)
- **根治“上下两个数字”缺陷**：重构了翻页渲染算法，使用 Canvas `clipRect` 与精确坐标对齐，彻底消除组件溢出，确保卡片呈现的始终是**单个数字在正中切断形成的上下两半**。
- **物理分体折叠动效**：翻页时上半页绕底轴铰链（0°→90°）自然折叠下翻，露出的新数字上部；新数字下半页以顶轴铰链（-90°→0°）平滑旋下贴合，并呈现金属铰链铆钉与暗部阴影。
- **完全汉化时钟标签**：中文模式下标签精准显示“时 / 分 / 秒”，英文模式显示“HOUR / MIN / SEC”。

### 2. 纯粹盲盒小憩博弈 (Blind-Box Nap Gamble)
- **完全隐藏时间**：小憩计时期间屏幕**彻底不显示任何时间走字或倒计时占位**，不给任何时间线索，完全考验人体生物钟直觉；
- **唯一动态交互按钮**：睡眠期间界面仅保留一个具有呼吸律动动画（850ms 循环脉冲放大）的交互按钮，随时醒来即可直接扣动扳机揭晓胜负。

### 3. 开源 3D 无光影左轮手枪 (CC0 1.0 3D Revolver Mesh)
- **套用优质开源 3D 模型**：集成来自 `Modbder/ThaumicBases` 的 Blender 3D 左轮手枪低模（严格遵循 **CC0 1.0 Universal 公共领域许可**，无版权风险）。
- **3/4 轴测纯平切面**：以纯原生 Compose Canvas 进行 3D 轴测透视与深度排序（Painter's Algorithm），多边形纯色无刺眼伪渐变，工业硬核美感直接悬浮于屏幕。
- **全动态联动**：点击左侧枪管即刻触发马格南开火与后坐力火光；点击右侧转轮触发弹仓真实 3D 旋转与机械齿轮音效。

### 4. 中文模式完全深度汉化
- 彻底排查并消除全部英文残留，底部导航栏（录音设闹、小憩轮盘、数据统计）、页面标题、设置选项、标签、按钮等 100% 实现自然地道中文表达。

### 5. 版本信息
- `versionCode`: `2`
- `versionName`: `0.1.1`

---

## 产物校验 (Artifacts)

| 文件名 | 文件类型 | 大小 | SHA256 校验和 |
|---|---|---|---|
| `app/build/outputs/apk/debug/app-debug.apk` | Debug APK | ~8.6MB | `9260651b412a5e6069d54b7e9c888cfb6c595276cfc5fc3d97a75ebabb82a248` |
| `app/build/outputs/apk/release/app-release-unsigned.apk` | Release APK | ~6.4MB | `469de37305a6068cea6f4d5c63a0b62de11c4174bbb1aff43e7ecb3c17d70129` |

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
