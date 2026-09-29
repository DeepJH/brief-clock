# Brief Clock (讲解时) - Release v1.1.0

## 版本概述 (Release Overview)

Brief Clock（中文名：**讲解时**）v1.1.0 带来了全面的视觉、交互与声效升级：解决了语言切换时的上下文安全崩溃问题，重构了上下分体机械翻页时钟（Split-Flap Clock），构建了 3D 轴测无光影左轮手枪，并对枪械声学引擎进行了物理多频段合成升级。

---

## 核心更新内容 (Changelog)

### 1. 稳定性与语言切换修复 (Bug Fixes)
- **修复语言切换闪退**：排查并修复了旧版本通过 `ContextImpl` 替换 `LocalContext` 导致的 `ClassCastException`（Activity 上下文丢失崩溃）。现在通过资源级动态应用与 Compose `LocalConfiguration` 联动，实现秒级无缝语言切换，100% 杜绝崩溃。
- **正式中文模式**：规范应用中文名为 **讲解时**，补全 `values-zh` 与 `values-zh-rCN` 全量本地化资源。

### 2. 真实双层机械翻页时钟 (Solari Split-Flap Clock)
- **拒绝伪翻转**：彻底重写翻页动效，构建了真正的上/下半页独立分体结构。
- **分阶段物理折叠**：
  - 前 50% 动画：上半页绕底部铰链轴测翻折下坠，背面逐渐显露新数字上部；
  - 后 50% 动画：下半页从铰链处平滑旋转贴合至下部，遮盖旧数字下半部；
  - 铰链分缝与铆钉机械细节，数字显示真实连贯，告别翻转变白与翻页失真。

### 3. 3D 轴测无光影悬浮左轮 (Unshaded 3D Revolver)
- **3/4 轴测多边形切面**：重写 `RevolverCanvas`，采用 3D 轴测透视构建枪管顶肋、侧壁、退壳杆下护套、3D 弧面机匣、击锤、扳机及双拼胡桃木握把。
- **纯粹无光影 (Flat Shading)**：摒弃模糊刺眼的伪渐变与伪阴影，采用实色色块拼合出工业硬核 3D 纵深感，直接悬浮在界面背景上，无外层卡片框约束。
- **动态交互**：转轮旋转时，6 颗膛孔沿 3D 椭圆轨道回旋，侧面凹槽同步位移；击锤张开、扳机位移、后坐力反冲及低多边形枪口爆燃火光全流程联动。

### 4. 物理声学枪械音效引擎 (Procedural DSP Audio)
- 基于 `AudioTrack` 纯程序化 PCM 实时合成，零第三方音频文件依赖：
  - **马格南枪声**：超音速初段破空 Transient + 850Hz 低通爆燃体 + 160Hz→38Hz 低音下潜 + 室内混响残响衰减 + 双曲正切饱和防破音；
  - **机械声效**：仿真单动击锤双阶锁定阻尼、转轮减速拨动与空仓撞针清脆脆鸣。
  - **静态 PCM 预加载**：应用启动后台预合成，点击扣扳机 0 延迟即刻发声。

### 5. Material Design 3 深度适配与主题
- 默认主色调设定为 **经典电光蓝 (Blue)**，内置深红、翠绿、幻紫、琥珀橙 5 种主题色。
- 全面支持跟随系统、浅色模式与深色模式。
- 移除非规范悬浮按钮， Brief Alert 设闹钟按钮移至底部并优化为宽幅胶囊（Pill Shape）形态，手动添加移入顶栏。
- 轮盘赌选时改造为上下阻尼滑动滚轮，规则抽离为轻量弹窗。

---

## 产物校验 (Artifacts)

| 文件名 | 文件类型 | 大小 | SHA256 校验和 |
|---|---|---|---|
| `app/build/outputs/apk/debug/app-debug.apk` | Debug APK | ~15MB | `09d53524fc8df1a278a1078e1ef2be969f537a7df646e2c9c93ee9e539b4ebae` |
| `app/build/outputs/apk/release/app-release-unsigned.apk` | Release APK | ~6.4MB | `efc0f283a52c0519e57e2859dab36fb98b35bcff5f529a91a1498aa0b102b5e7` |

---

## 快速安装与运行 (Installation)

```bash
# 1. 使用 Gradle 直接安装到连接的设备
./gradlew installDebug

# 2. 启动应用
adb shell am start -n com.briefclock.app.debug/com.briefclock.app.MainActivity

# 或使用 adb 直接推送安装 APK
adb install -r -t app/build/outputs/apk/debug/app-debug.apk
```
