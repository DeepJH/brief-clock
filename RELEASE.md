# Brief Clock (讲解时) - v0.1.6

## 免责声明 (Disclaimer)

> **特别声明（无政治立场）**：  
> 本应用中文名称“讲解时 (Brief Clock)”及相关调侃梗纯属同音谐音趣味调侃，旨在为作息习惯与小憩自律增添乐趣，与任何历史或现实政治人物无关。开发者及本应用不代表、亦不持任何政治立场，特此声明，请勿进行任何政治联想或过度解读。  
> 
> **Disclaimer (Non-Political)**:  
> The app name '讲解时' (Brief Clock) and associated humor are purely linguistic puns for entertainment and habit building, with no connection to any historical or political figures. The developers and this application hold no political stance.

---

## 版本概述 (Release Overview)

Brief Clock (讲解时) 正式发布 **Brief Clock (讲解时)-v0.1.6**！本次版本针对小睡轮盘、左轮手枪细节、底部栏导航以及迷幻彩蛋小游戏进行了全面体验升级与精细化打磨：
1. **左轮手枪人体工学与材质机械重构**：
   - **扳机左右方向与扣动纠正**：纠正了反向装配，左侧迎指面呈现经典内凹人体工学弧线，背部微凸；开火时向后方握把自然收紧（-14°）。
   - **古典胡桃木棕色握把**：去除了原握把上突兀的灰色曲线长条，整体换装温润纯正的胡桃木棕色。
   - **锐利挺拔的下端棱角**：重构握把底部线条，前下角（toe）与后下角（heel）具备清晰锐利的棱角与平直底托。
   - **击锤整体逆时针复位**：默认状态整体逆时针旋转 20° 回归未待击发休息位置（平伏于机框上方），进入待击发时顺畅向后张开至待发位置。
2. **小睡轮盘界面上下空间优化**：
   - 将左轮展示区域大幅扩大（高度提升至 300dp，宽度占比达 96%），居中展示更加大气磅礴；
   - 将下半部定时选择区域（轮盘选择器高度缩至 105dp、开赌按钮高度缩至 46dp 及各处间距）**缩减至原本的 80%**，彻底解决左轮区域过于局促的问题。
3. **底部导航栏小左轮图标重做**：
   - 取代原有手写形变矢量，直接提取改好后的大左轮真实外轮廓，生成等比缩放的 24dp 极简单色图标（`ic_nav_revolver.png`），自适应未选中沉稳灰与选中主题蓝。
4. **迷幻风击碎困意彩蛋游戏优化**：
   - **绽放礼花动画修复**：加入帧刷新驱动机制，倒计时归零后的终结礼花雨真实自中心向四周喷洒并在重力下漫天绽放，彻底告别静止冻结。
   - **弹窗字体比例微调**：`本次击中xx个` 字号由 32sp 缩至 20sp，确保在弹窗内单行完整排版、绝不换行；小游戏挑战结束标题字号由 22sp 缩至 17sp，精致和谐。
   - **emoji 飞行速度减半**：三款表情飞行速度降至原本的 0.5 倍速，视觉穿梭感平稳舒适。

---

## 核心更新内容 (Changelog v0.1.6)

### 1. 左轮手枪细节精细化
- 扳机左右翻转纠正：迎指面内凹弧线朝左，尖端朝下前，扣扳机时向后方握把旋转扣合。
- 握把视觉提升：去除灰色长条高光线，改为古典胡桃木棕色；下端底板方正硬朗，前后双角更锐利。
- 击锤逆时针归位：未待击发时平贴于机框后上方，待击发状态向后自然张开。

### 2. 小睡轮盘布局重新分配
- 左轮画布高度从 230dp 提升至 300dp，居中展示。
- 定时滚轮 picker 与启动按钮缩小至原本 80%，布局舒展协调。

### 3. 底部栏导航图标等比套用
- 换装基于新左轮轮廓的 `ic_nav_revolver.png`，完美保持真实左轮的枪管、弹仓、扳机与方底握把特征。

### 4. 击碎困意彩蛋游戏体验修复
- 礼花效果修复为 60 FPS 动态喷发绽放，粒子在结果弹窗前面层级绽放散落。
- `本次击中xx个` 字体调优为 20sp 单行不换行，挑战结束标题调整为 17sp。
- 瞌睡表情飞行速度调整为 0.5 倍速。

### 5. 版本信息
- `versionCode`: `7`
- `versionName`: `0.1.6`

---

## 产物校验 (Artifacts)

| 文件名 | 文件类型 | 大小 | SHA256 校验和 |
|---|---|---|---|
| `app/build/outputs/apk/debug/app-debug.apk` | Debug APK | ~11MB | `5d75eee0983e04296b527edf565f8c86a5fd4cebd85d572c9b2eda706c43c847` |
| `app/build/outputs/apk/release/app-release-unsigned.apk` | Release APK | ~7.7MB | `1169b5f59cf3a6ff12d7573dbd8367fe5fcd6e02b2a4e300994c99673615ed97` |

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
