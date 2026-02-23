# Lumin Graphics

**English** | [简体中文](README_zh.md)

Lumin Graphics is a lightweight, high-performance rendering framework 
designed for modern Minecraft modding.


### IMPORTANT: The English version may occasionally be out of sync.

---

## Features
- **SDF Rounded Rectangles**: Smooth, anti-aliased corners calculated via 
fragment shaders. Supports dynamic radii without vertex deformation.
- **High-Performance TTF**: Advanced TrueType Font rendering with atlas-based batching,
significantly reducing Draw Calls.

Here is the professional English translation and optimization of your **Quick Start** section. I have refined the technical terms to better match Minecraft modding standards and the "Lumin" aesthetic.

---

[!CAUTION]

## 💡 WARNING

### Lifecycle Synchronization Constraints

Within a single frame's render loop, it is strictly prohibited to call 
`Renderer.clear()` followed by another `draw()` on the same instance after 
a `draw()` has already been executed. This sequence causes severe 
synchronization conflicts between CPU writes and GPU reads, leading to 
visual flickering or the disappearance of primitives.

If your business logic requires multiple cycles of clearing and drawing within a single frame:

Please instantiate a new Renderer to handle subsequent tasks.

**💡 NOTE**: Please avoid creating excessive Renderer instances, as this will 
lead to over-allocation of VRAM. Alternatively, you may specify a smaller Buffer size 
than the default value when instantiating a Renderer to mitigate memory overhead.

---

## Quick Start

Lumin Graphics performs all rendering through specialized **Renderers**.

### Available Renderers

* `RectRenderer`: Optimized for standard flat rectangles.
* `RoundRectRenderer`: For anti-aliased rectangles with dynamic corner radii.
* `TtfTextRenderer`: For high-performance TrueType font rendering.
* `TextureRenderer`：For drawing different textures.

### Initialization & Thread Safety

Renderers **must** be initialized on the **Render Thread**. We recommend using `Suppliers.memoize` (from Guava/Minecraft) to ensure safe, lazy initialization.

```java
// Recommended initialization
private final Supplier<RectRenderer> rectRenderer = Suppliers.memoize(RectRenderer::new);

// Use .get() to access the renderer instance
rectRenderer.get().addRect(10f, 10f, 100f, 100f, Color.WHITE);

```

---

### Usage Patterns

#### 1. Basic Draw & Reset

For most immediate-mode UI tasks, you will add shapes and clear the buffer in the same frame:

```java
// 1. Add shapes to the buffer
rectRenderer.get().addRect(10f, 10f, 200f, 200f, Color.WHITE);

// 2. Draw to screen and clear data for the next frame
rectRenderer.get().draw();
rectRenderer.get().clear(); 
// You can use drawAndClear() to instead

```

#### 2. Buffer Reusability

Lumin Renderers are designed for **persistent buffers**. If your UI doesn't change every frame, you can add vertices once and draw them multiple times across frames to save CPU cycles.

```java
// In your init or first frame:
rectRenderer.get().addRect(10f, 10f, 200f, 200f, Color.CYAN);

// In your rendering loop:
rectRenderer.get().draw(); // The content remains in the GPU buffer until .clear() is called.

```

---

### 💡 Optimization Note

Since you are using **Lumin Graphics**, remember that calling `.draw()` multiple times without `.clear()` is extremely efficient as it simply re-triggers the draw call on existing GPU data without re-uploading vertices.

---

## License

- **Lumin Graphics**: The core rendering components (located in 
`src/main/java/com/github/lumin/graphics/`) are licensed under the
[MIT License](LICENSE).

---
Copyright © 2026 slmpc.