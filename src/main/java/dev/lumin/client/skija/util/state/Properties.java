package dev.lumin.client.skija.util.state;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Properties {
    private int[] lastUnpackSkipImages = new int[1];
    private int[] lastUnpackImageHeight = new int[1];
    private int[] lastPackSkipImages = new int[1];
    private int[] lastPackImageHeight = new int[1];
    private int[] lastUnpackSkipRows = new int[1];
    private int[] lastUnpackSkipPixels = new int[1];
    private int[] lastUnpackRowLength = new int[1];
    private int[] lastUnpackAlignment = new int[1];
    private int[] lastUnpackLsbFirst = new int[1];
    private int[] lastUnpackSwapBytes = new int[1];
    private int[] lastPackAlignment = new int[1];
    private int[] lastPackSkipRows = new int[1];
    private int[] lastPackSkipPixels = new int[1];
    private int[] lastPackRowLength = new int[1];
    private int[] lastPackLsbFirst = new int[1];
    private int[] lastPackSwapBytes = new int[1];
    private int[] lastActiveTexture = new int[1];
    private int[] lastProgram = new int[1];
    private int[] lastTexture = new int[1];
    private int[] lastSampler = new int[1];
    private int[] lastArrayBuffer = new int[1];
    private int[] lastVertexArrayObject = new int[1];
    private int[] lastPolygonMode = new int[2];
    private int[] lastViewport = new int[4];
    private int[] lastScissorBox = new int[4];
    private int[] lastBlendSrcRgb = new int[1];
    private int[] lastBlendDstRgb = new int[1];
    private int[] lastBlendSrcAlpha = new int[1];
    private int[] lastBlendDstAlpha = new int[1];
    private int[] lastBlendEquationRgb = new int[1];
    private int[] lastBlendEquationAlpha = new int[1];
    private int[] lastPixelUnpackBufferBinding = new int[1];
    private boolean lastEnableBlend;
    private boolean lastEnableCullFace;
    private boolean lastEnableDepthTest;
    private boolean lastEnableStencilTest;
    private boolean lastEnableScissorTest;
    private boolean lastEnablePrimitiveRestart;
    private boolean lastDepthMask;
}
