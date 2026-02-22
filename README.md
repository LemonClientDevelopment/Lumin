# Lumin Client - ClickGUI 设计文档

**项目名称**: Lumin Client
**目标平台**: NeoForge 1.21.11
**文档版本**: v1.0
**设计风格**: 3D 圆柱滚筒交互 (Cylindrical Roller) + 极简主义 (Minimalist)

## 1. 视觉风格指南 (Visual Identity)

Lumin 的核心视觉语言是“光”与“深度”。界面应当看起来像是悬浮在 Minecraft 世界之上的全息投影。

### 1.1 配色方案 (Color Palette)
*   **背景 (Background)**: `#050505` (近乎纯黑，带极低透明度的高斯模糊 Background Blur)。
*   **主色调 (Primary)**: `#FFFFFF` (纯白，用于高亮和选中项，呼应 "Lumin")。
*   **次色调 (Secondary)**: `#808080` (中灰，用于未选中项)。
*   **强调色 (Accent)**: `#00D2FF` (青色微光，用于模块开启状态或滚动条指示)。
*   **文字阴影**: 使用轻微的 `Drop Shadow` 增加悬浮感。

---

## 2. 核心交互设计：圆柱滚筒 (The Cylinder Roller)

这是 Lumin ClickGUI 的灵魂所在。我们将传统的横向 Category 排列（Combat, Movement, Misc...）映射到一个不可见的 3D 圆柱体表面。

### 2.1 布局概念
想象一个横放的巨大圆柱体（像易拉罐横放），悬浮在屏幕中央偏上的位置。
*   **Category 排列**: Combat, Movement, Misc, Visual, Client 等分类 evenly spaced（均匀分布）贴在圆柱体表面。
*   **视口 (Viewport)**: 玩家只能看到圆柱体正对屏幕的那一部分（约 90 度视角范围）。

### 2.2 交互逻辑 (Interaction Logic)
*   **滚动机制**:
    *   **鼠标操作**: 按住鼠标左键/右键在屏幕任意位置水平拖动。
    *   **物理反馈**:
        *   鼠标向 **左** 移动 (`deltaX < 0`) -> 圆柱体表面向 **左** 移动 (视觉上滚筒逆时针旋转，露出右侧内容)。
        *   鼠标向 **右** 移动 (`deltaX > 0`) -> 圆柱体表面向 **右** 移动。
    *   **惯性 (Inertia)**: 松开鼠标后，滚筒会根据最后的速度继续滑动一段距离，并带有摩擦力减速，最终吸附（Snap）到最近的一个 Category 上。

### 2.3 视觉特效 (Visual Effects)
为了体现 3D 感，必须处理透视：
1.  **中心聚焦**: 位于屏幕正中央（X轴中点）的 Category 显示为 **100% 大小**，颜色为 **纯白 (Semibold)**，带有发光效果。
2.  **边缘衰减**:
    *   随着 Category 向左右两侧移动，其 **透明度 (Alpha)** 逐渐降低 (1.0 -> 0.3)。
    *   **缩放 (Scale)** 逐渐变小 (1.0 -> 0.7)。
    *   **模糊 (Blur)** 逐渐增加（模拟景深）。
    *   字体颜色从白色渐变为深灰色。

---

## 3. 界面层级结构 (UI Hierarchy)

ClickGUI 分为三个层级，通过点击操作深入。

### Level 1: 滚筒选择层 (The Roller)
*   **位置**: 屏幕垂直居中偏上 (Y = Height * 0.35)。
*   **内容**: 显示所有 Category 名称。
*   **操作**: 拖拽滚动，点击选中。

### Level 2: 模块列表层 (Module List)
*   **触发**: 当某个 Category 被选中并停在正中央时。
*   **位置**: 滚筒正下方，以半透明卡片形式展开。
*   **布局**: 传统的两列或三列网格布局 (Grid Layout)，简洁风格。
*   **动画**: 当滚筒转动切换 Category 时，下方的模块列表应当有一个 **向上滑动消失 -> 内容切换 -> 向下滑入** 的过渡动画，或者淡入淡出。

### Level 3: 设置弹窗 (Settings Modal)
*   **触发**: 点击具体的 Module。
*   **样式**: 悬浮在模块列表之上的小窗口。
*   **内容**: Boolean (Switch), Slider, Keybind, Mode (Dropdown)。

---

## 4. 技术实现方案 (Technical Implementation for NeoForge 1.21.1)

### 4.1 渲染引擎 (Rendering)
由于需要 3D 透视效果，不能仅使用标准的 2D GUI 绘制。
*   **MatrixStack**: 必须使用 `PoseStack` 进行推栈和弹栈操作。
*   **3D 投影计算**:
    对于每个 Category 文本，计算其在圆柱体上的角度 $\theta$。
    *   $x_{screen} = centerX + R \cdot \sin(\theta)$
    *   $z_{depth} = R \cdot \cos(\theta)$ (用于计算缩放和透明度)
    *   $scale = \text{lerp}(minScale, maxScale, z_{depth})$
*   **GLSL Shaders**: 编写一个简单的 GUI Shader 来处理边缘模糊和发光效果，这比 CPU 计算模糊要快得多。

## 6. 开发路线图 (Roadmap)

1.  **Phase 1: 基础框架**: 浏览项目。
2.  **Phase 2: 滚筒核心**: 实现 `CylinderRenderer`，完成 3D 数学计算和 MatrixStack 变换，实现鼠标拖拽滚动。
3.  **Phase 3: 业务逻辑**: 接入 Module 系统，实现 Category 与 Module 以及 Settings 的关联，完成 Level 2 列表的展开动画。
4.  **Phase 4: 美化**: 添加高斯模糊背景 (Blur Shader)，以及其他美化Shader，添加点击音效。

---

**附注**: 优先保证成品质量而不是速度。