# Lumin

基于 NeoForge 的客户端 Mod 工程，包含一套面向现代 Blaze3D 的 GUI 渲染框架（Lumin Graphics），以及正在移植中的 Jello 风格 ClickGUI。

## 构建

```bash
./gradlew classes
```

## Lumin Graphics

渲染框架代码位于：[graphics](file:///c:/Users/L3MonKe/Desktop/Code/Lumin/src/main/java/com/github/lumin/graphics)

要点：
- 立即模式 API（先 add，再 draw / drawAndClear）
- 持久化映射顶点缓冲（减少重复上传）
- RenderPipeline + RenderPass 的现代 Blaze3D 绘制路径

框架说明见：[graphics/README.md](file:///c:/Users/L3MonKe/Desktop/Code/Lumin/src/main/java/com/github/lumin/graphics/README.md)

## ClickGUI（Jello 移植）

入口模块：`ClickGui`（默认按键：Right Shift）
- 模块位置：[ClickGui](file:///c:/Users/L3MonKe/Desktop/Code/Lumin/src/main/java/com/github/lumin/modules/impl/client/ClickGui.java)
- Screen 实现：[ClickGuiScreen](file:///c:/Users/L3MonKe/Desktop/Code/Lumin/src/main/java/com/github/lumin/gui/clickgui/ClickGuiScreen.java)

当前实现的基础能力：
- 分类面板布局（按 `Category` 生成）
- 面板拖拽
- 模块列表（左键开关）
- 右键打开设置弹层（当前仅展示 setting 名称与值）
- 鼠标滚轮滚动（按可视区域裁剪渲染行）

## Jello 资源

已复制的 Jello 贴图与字体位于：
- `src/main/resources/assets/lumin/jello/`

## 贴图渲染器

贴图渲染通过 `TextureRenderer` 完成（按 `Identifier` 分批并缓存上传）：
- [TextureRenderer](file:///c:/Users/L3MonKe/Desktop/Code/Lumin/src/main/java/com/github/lumin/graphics/renderers/TextureRenderer.java)

示例（在 GUI 渲染中绘制一张图）：

```java
var tex = new com.github.lumin.graphics.renderers.TextureRenderer();
tex.addTexture("jello/JelloPanel.png", 50, 50, 119, 169);
tex.drawAndClear();
```

