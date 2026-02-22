# ClickGUI 设计灵感与参考总结

本文档总结了 `SigmaJello`, `Sakura`, `ThunderHack-Recode` 三个优秀开源项目的 ClickGUI 设计思路，旨在为 **Lumin Client** 的开发提供视觉效果、尺寸规范和交互设计的参考。

---

## 1. SigmaJello (经典 Jello 风格)
**核心风格**: 拟物化 (Skeuomorphism) / 材质感 / 弹性动画

### 🎨 视觉特效 (Visual Effects)
*   **材质感**: 大量使用半透明 PNG 图片素材（阴影、高光、圆角遮罩）来模拟物理质感，而非纯代码绘制。
*   **弹性动画 (Spring/Bounce)**: 
    *   **核心**: 界面展开、滑块拖动均带有显著的“果冻”弹性回弹效果 (Bouncing)。
    *   **实现**: 使用 `smoothTrans` 函数进行插值计算，模拟物理惯性。
*   **颜色动态**: 
    *   **悬停**: 亮度提升 (`0.8f + 0.16f * hoverTrans`)。
    *   **选中**: 标志性的 Jello 青蓝色 (`#2AA5FF`)。

### 📏 尺寸参数 (Dimensions)
*   **Panel 宽度**: `119px` (内容区域约 `100px`，含阴影素材边距)。
*   **Panel 间距**: `5px`。
*   **Header 高度**: `30px`。
*   **Module 高度**: `15px` (紧凑型列表)。
*   **组件尺寸**: 
    *   Checkbox: `12x12px`。
    *   Slider 宽度: 约 `41px`。

### 🖱️ 交互设计 (Interaction)
*   **右键属性弹窗 (Popup)**: Module 的详细设置（Slider, Checkbox）不直接在列表下方展开，而是**弹出一个新的悬浮窗**，通过动态连线或动画与父级关联。
*   **物理滚动**: 列表滚动带有明显的惯性，手感顺滑。

---

## 2. Sakura (现代极简风格)
**核心风格**: 扁平化 (Flat) / 光影感 (Bloom) / 矢量绘图

### 🎨 视觉特效 (Visual Effects)
*   **实时模糊 (Real-time Blur)**: 
    *   **核心**: 广泛使用 `BlurShader` 对背景或 Panel 进行高斯模糊处理，提升层次感。
    *   **光晕 (Bloom)**: 选中项或激活模块带有柔和的外发光效果。
*   **矢量绘图**: 依赖 NanoVG 或类似库进行高质量的矢量图形渲染，确保在不同分辨率下清晰锐利。
*   **缓动动画**: 使用 `EaseOutSine` 或 `EaseInOutQuad` 曲线，强调展开/收起的节奏感，比线性动画更自然。

### 📏 尺寸参数 (Dimensions)
*   **Panel 宽度**: `110px` (随 GUI Scale 缩放)。
*   **Header 高度**: `18px`。
*   **圆角半径**: `7px` (统一的大圆角风格)。
*   **间距**: Panel 之间间距 `10px`。
*   **图标**: 矢量图标大小通常为字号的 `1.5` 倍。

### 🖱️ 交互设计 (Interaction)
*   **手风琴折叠**: 点击 Header 展开/收起 Module 列表，配合 EaseOut 动画。
*   **全局滚动**: 支持鼠标滚轮直接滚动整个屏幕的内容。

---

## 3. ThunderHack-Recode (黑客/硬核风格)
**核心风格**: 赛博朋克 / 故障风 (Glitch) / 一体化窗口

### 🎨 视觉特效 (Visual Effects)
*   **深色玻璃**: 深色半透明背景，配合 Glitch 字体效果和渐变色块。
*   **阴影模糊**: 使用 `Render2DEngine` 绘制带颜色的模糊阴影 (`drawBlurredShadow`)，增加立体感。
*   **回弹窗口**: 窗口打开时使用 `EaseOutBack`，带有明显的“超出再回弹”动画。
*   **主题色系统**: 支持基于索引的动态配色 (`getColorByTheme`)，整体色调统一且可变。

### 📏 尺寸参数 (Dimensions)
*   **主窗口**: `400px` (宽) x `250px` (高) (可拖拽调整)。
*   **侧边栏**: `90px` (Category 列表)。
*   **Module 板块高度**: `35px` (较大，便于点击)。
*   **圆角半径**: 
    *   主窗口: `9px`
    *   内部板块: `7px`
    *   按钮: `4px`

### 🖱️ 交互设计 (Interaction)
*   **单窗口设计 (One-Window)**: 类似 CSGO 辅助菜单，左侧导航，右侧内容。
*   **功能集成**: 底部集成 Config Manager 和 Friend Manager。
*   **搜索功能**: 顶部内置搜索框，实时过滤 Module。
*   **侧边设置栏**: 点击 Module 后，设置项在窗口的最右侧区域展开，布局清晰。

---

## 4. 对 Lumin Client 的建议 (Adaptation)

结合 Lumin 的 **3D 圆柱滚筒 (Cylindrical Roller)** 设计目标，我们可以借鉴以下几点：

### ✅ 渲染技术 (Rendering)
1.  **Shader 模糊 (来自 Sakura)**: 
    *   圆柱体边缘的衰减和模糊必须使用 **Fragment Shader** 实现，CPU 计算开销过大。
    *   参考 Sakura 的 `BlurShader` 实现背景的高斯模糊。
2.  **发光效果 (来自 Sakura/ThunderHack)**:
    *   位于圆柱体正中央的选中项，应添加 **Bloom/Glow** 效果，使其看起来像是全息投影。

### ✅ 交互体验 (UX)
1.  **惯性滚动 (来自 SigmaJello)**:
    *   滚筒的旋转必须带有 **物理惯性 (Inertia)**，参考 Jello 的 `smoothTrans` 算法，让滚动的起止更加自然。
2.  **弹性反馈 (来自 SigmaJello)**:
    *   当滚筒快速旋转停止时，可以加入微小的 **回弹 (Bounce)** 动画，增加实物感。

### ✅ 布局细节 (Layout)
1.  **矢量图标 (来自 Sakura)**:
    *   在圆柱体上的 Category 标题旁，添加简洁的矢量图标，随 3D 透视进行缩放而不失真。
2.  **层级展开 (来自 ThunderHack)**:
    *   Lumin 的 Level 2 (Module 列表) 可以借鉴 ThunderHack 的 **Scissor 裁剪** 技术，确保列表在展开/滚动时不会溢出指定的显示区域。

---
*文档生成时间: 2026-02-22*
