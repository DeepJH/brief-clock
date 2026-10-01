# Brief Clock (讲解时) - v0.1.3

## 免责声明 (Disclaimer)

> **特别声明（无政治立场）**：  
> 本应用中文名称“讲解时 (Brief Clock)”及相关调侃梗纯属同音谐音趣味调侃，旨在为作息习惯与小憩自律增添乐趣，与任何历史或现实政治人物无关。开发者及本应用不代表、亦不持任何政治立场，特此声明，请勿进行任何政治联想或过度解读。  
> 
> **Disclaimer (Non-Political)**:  
> The app name '讲解时' (Brief Clock) and associated humor are purely linguistic puns for entertainment and habit building, with no connection to any historical or political figures. The developers and this application hold no political stance.

---

## 版本概述 (Release Overview)

Brief Clock (讲解时) 正式发布 **Brief Clock (讲解时)-v0.1.3**！本次版本带来了严谨真实的经典左轮手枪矢量重构（彻底告别套筒手枪与弹匣，回归真正 6 槽弹巢与击锤）、方锐中空蓝白极简六边形沙漏新图标、小睡滚轮初始定位 10s、彩蛋小游戏（白底、渐进扩散色圈、表情渐显飞出、置顶彩带与大号居中确认按钮）及误触即消逻辑。

---

## 核心更新内容 (Changelog v0.1.3)

### 1. 经典左轮手枪结构严谨重构
- **严正纠正枪械结构**：彻底排除任何套筒或弹匣概念，严格基于 Game-Icons 经典左轮矢量（CC-BY 3.0 许可），展现真实 6 槽圆柱转轮弹巢（Cylinder）、机械击锤（Hammer）、弧形扳机及扳机护圈、退壳杆下护套与实木握把。
- **直觉交互**：点击左半侧枪管开火（马格南枪声+后坐力仰角+枪口火光）；点击右半侧转轮旋转弹仓（清脆齿轮音效）。

### 2. 应用图标重构（方锐中空感叹号 + 矩形底盖沙漏）
- **方锐中空**：感叹号改用方正凌厉的硬角造型，且中部掏空处理，极致硬核。
- **矩形沙漏底盖**：横向沙漏左右两端采用利落的矩形底盖，漏斗为极简直线。
- **自然隐形六边形**：移除了生硬的浅蓝框线，凭借感叹号顶点与沙漏矩形底盖的几何点位，自然形成外切正六边形视觉张力；四周放大留白，视野更开阔。

### 3. 小睡轮盘初始选时调整
- 小睡选时滚轮初始加载位置直接定位在 **10s (测试)** 档位，方便秒级测试与体验完整轮盘判定。

### 4. 击碎困意彩蛋游戏重做与交互修复
- **严格点击即消**：10 秒内点左轮 10 次召唤 `💤`；点击其他任何区域 `💤` 立即消失，点中 `💤` 触发转轮声与上下撕裂进入彩蛋。
- **白底与渐进加速色圈**：游戏初始背景为纯白，随后中心向外涌出柔和模糊的多色光环，节奏由慢逐渐加快，迷幻向前穿梭。
- **表情渐显飞出**：瞌睡表情从中心出发时完全透明，向外移动中逐渐渐变显现，边飞边显。
- **置顶礼花与大号居中按钮**：结算时彩带烟花置顶散落，成绩弹窗配有大号居中“太棒了”确认按钮，确认后界面平滑合拢复原。

### 5. 版本信息
- `versionCode`: `4`
- `versionName`: `0.1.3`

---

## 产物校验 (Artifacts)

| 文件名 | 文件类型 | 大小 | SHA256 校验和 |
|---|---|---|---|
| `app/build/outputs/apk/debug/app-debug.apk` | Debug APK | ~11MB | `cd5544d2b055b3fe17c9ab9acb4f5755306eecbe98f18fa41b71dcc4b694544c` |
| `app/build/outputs/apk/release/app-release-unsigned.apk` | Release APK | ~7.6MB | `9b1068486d40334e198deb9ce0ec27b396faa5d7f0537ade842c34fe7de56098` |

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
