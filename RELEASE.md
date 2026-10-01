# Brief Clock (讲解时) - v0.1.3

## 免责声明 (Disclaimer)

> **特别声明（无政治立场）**：  
> 本应用中文名称“讲解时 (Brief Clock)”及相关调侃梗纯属同音谐音趣味调侃，旨在为作息习惯与小憩自律增添乐趣，与任何历史或现实政治人物无关。开发者及本应用不代表、亦不持任何政治立场，特此声明，请勿进行任何政治联想或过度解读。  
> 
> **Disclaimer (Non-Political)**:  
> The app name '讲解时' (Brief Clock) and associated humor are purely linguistic puns for entertainment and habit building, with no connection to any historical or political figures. The developers and this application hold no political stance.

---

## 版本概述 (Release Overview)

Brief Clock (讲解时) 正式发布 **Brief Clock (讲解时)-v0.1.3**！本次版本带来了严谨真实的经典左轮手枪全配件重构（彻底告别套筒手枪与弹匣，回归真正 6 槽弹巢、击锤、弧形扳机与上仰后坐力）、多轮迭代淬炼的方锐微收感叹号与矩形沙漏自然六边形新图标、小睡滚轮初始定位 10s、迷幻毒蘑菇风彩蛋小游戏（白底、加粗不规则柔和色圈、加速涌出、表情渐显平滑飞出、置顶彩带与大号居中确认按钮）及非阻断即消逻辑。

---

## 核心更新内容 (Changelog v0.1.3)

### 1. 经典左轮手枪结构严谨重构
- **严正纠正枪械构造**：彻底排除任何套筒或弹匣概念，严格基于 Wikimedia Commons 经典左轮矢量（CC BY-SA 3.0 / F l a n k e r 许可），展现真实 6 槽圆柱转轮弹巢（Cylinder）、外露机械击锤（Hammer）、弧形银色扳机及扳机护圈、退壳杆下护套与人体工学木纹握把。
- **物理动效纠正**：点击左半侧枪管开火（枪口向上昂首后仰仰角反冲 + 枪尖多角星芒爆燃烈焰 + 马格南枪声）；点击右半侧转轮旋转弹仓（清脆齿轮音效 + 转轮凹槽动态光影回旋）。

### 2. 应用图标重构（方锐微收感叹号 + 矩形底盖沙漏，多轮迭代）
- **方锐微收感叹号**：参考经典硬核字形几何比例，采用平顶平底、下端微微收窄的实心电光蓝造型，搭配小巧锐利的下方正方点，重心适度下移，和谐稳重。
- **矩形沙漏底盖**：横向沙漏左右两端采用粗壮利落的矩形盖板，漏斗为极简加粗直线，连接处完全平整无毛刺。
- **自然隐形六边形**：无生硬外框线，凭借感叹号顶部、底点与沙漏左右矩形底盖的 4 个顶角，在视觉完形心理上自然闭合出正六边形张力；四周放大留白，优雅通透。

### 3. 小睡轮盘初始选时调整
- 小睡选时滚轮初始加载位置直接定位在 **10s (测试)** 档位，方便秒级测试与体验完整轮盘判定。

### 4. 击碎困意彩蛋游戏迷幻重做与非阻断修复
- **非阻断点击即消**：10 秒内点左轮 10 次召唤 `💤`；点击其他任何区域 `💤` 立即消失的同时正常触发该区域自身操作（如点枪开火、点开赌生效），点中 `💤` 触发转轮声与上下撕裂进入彩蛋。
- **吃毒蘑菇般的迷幻色圈**：游戏初始背景为纯净白底，中心向外涌出加粗一倍、柔和高饱和度（洋红、荧光青、酸绿、亮紫）的不规则蠕动光环，初始缓慢悠长，随后平滑加速至 3 倍速。
- **表情渐显连续平滑飞出**：瞌睡表情从中心出发时完全透明，在 0.15~0.45 秒内向外连续 60 帧平滑飞出并渐变显现，边飞边显。
- **置顶礼花与大号居中按钮**：结算时彩带烟花直接置顶散落在结果弹窗前面，弹窗配有大号居中“太棒了”确认按钮，确认后界面平滑合拢复原。

### 5. 版本信息
- `versionCode`: `4`
- `versionName`: `0.1.3`

---

## 产物校验 (Artifacts)

| 文件名 | 文件类型 | 大小 | SHA256 校验和 |
|---|---|---|---|
| `app/build/outputs/apk/debug/app-debug.apk` | Debug APK | ~11MB | `d8722d2320a1b5857d49ad92173e7891359024a640002fd60ed1ec0dbe54d3d3` |
| `app/build/outputs/apk/release/app-release-unsigned.apk` | Release APK | ~7.6MB | `0e66209b22da12ef1f3309d208e75177eea4ae2fcc85f4ff1824f1e2d2d79848` |

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
