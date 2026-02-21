package dev.lumin.client.skija.util.state;

import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.GL_PIXEL_UNPACK_BUFFER;
import static org.lwjgl.opengl.GL21.GL_PIXEL_UNPACK_BUFFER_BINDING;
import static org.lwjgl.opengl.GL30.GL_VERTEX_ARRAY_BINDING;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.GL_PRIMITIVE_RESTART;
import static org.lwjgl.opengl.GL33.GL_SAMPLER_BINDING;
import static org.lwjgl.opengl.GL33.glBindSampler;


public final class State {
    private final int glVersion;
    private final Properties props;

    public State(int glVersion) {
        this.glVersion = glVersion;
        this.props = new Properties();
    }

    /**
     * Saves the current OpenGL state.
     * <p>
     * This code was inspired by [imgui-java](https://github.com/SpaiR/imgui-java/blob/2a605f0d8500f27e13fa1d2b4cf8cadd822789f4/imgui-lwjgl3/src/main/java/imgui/gl3/ImGuiImplGl3.java#L398-L425)
     * and modified to fit the project's codebase.
     *
     * @see #pop()
     */
    public State push() {
        glGetIntegerv(GL_ACTIVE_TEXTURE, props.getLastActiveTexture());
        glActiveTexture(GL_TEXTURE0);
        glGetIntegerv(GL_CURRENT_PROGRAM, props.getLastProgram());
        glGetIntegerv(GL_TEXTURE_BINDING_2D, props.getLastTexture());
        if (glVersion >= 330 || GL.getCapabilities().GL_ARB_sampler_objects) {
            glGetIntegerv(GL_SAMPLER_BINDING, props.getLastSampler());
        }
        glGetIntegerv(GL_ARRAY_BUFFER_BINDING, props.getLastArrayBuffer());
        glGetIntegerv(GL_VERTEX_ARRAY_BINDING, props.getLastVertexArrayObject());
        if (glVersion >= 200) {
            glGetIntegerv(GL_POLYGON_MODE, props.getLastPolygonMode());
        }
        glGetIntegerv(GL_VIEWPORT, props.getLastViewport());
        glGetIntegerv(GL_SCISSOR_BOX, props.getLastScissorBox());
        glGetIntegerv(GL_BLEND_SRC_RGB, props.getLastBlendSrcRgb());
        glGetIntegerv(GL_BLEND_DST_RGB, props.getLastBlendDstRgb());
        glGetIntegerv(GL_BLEND_SRC_ALPHA, props.getLastBlendSrcAlpha());
        glGetIntegerv(GL_BLEND_DST_ALPHA, props.getLastBlendDstAlpha());
        glGetIntegerv(GL_BLEND_EQUATION_RGB, props.getLastBlendEquationRgb());
        glGetIntegerv(GL_BLEND_EQUATION_ALPHA, props.getLastBlendEquationAlpha());
        props.setLastEnableBlend(glIsEnabled(GL_BLEND));
        props.setLastEnableCullFace(glIsEnabled(GL_CULL_FACE));
        props.setLastEnableDepthTest(glIsEnabled(GL_DEPTH_TEST));
        props.setLastEnableStencilTest(glIsEnabled(GL_STENCIL_TEST));
        props.setLastEnableScissorTest(glIsEnabled(GL_SCISSOR_TEST));
        if (glVersion >= 310) {
            props.setLastEnablePrimitiveRestart(glIsEnabled(GL_PRIMITIVE_RESTART));
        }

        // This state is not saved in the original imgui-java project but is included to address bugs encountered when drawing with Skija.
        props.setLastDepthMask(glGetBoolean(GL_DEPTH_WRITEMASK));

        glGetIntegerv(GL_PIXEL_UNPACK_BUFFER_BINDING, props.getLastPixelUnpackBufferBinding());
        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);

        glGetIntegerv(GL_PACK_SWAP_BYTES, props.getLastPackSwapBytes());
        glGetIntegerv(GL_PACK_LSB_FIRST, props.getLastPackLsbFirst());
        glGetIntegerv(GL_PACK_ROW_LENGTH, props.getLastPackRowLength());
        glGetIntegerv(GL_PACK_SKIP_PIXELS, props.getLastPackSkipPixels());
        glGetIntegerv(GL_PACK_SKIP_ROWS, props.getLastPackSkipRows());
        glGetIntegerv(GL_PACK_ALIGNMENT, props.getLastPackAlignment());

        glGetIntegerv(GL_UNPACK_SWAP_BYTES, props.getLastUnpackSwapBytes());
        glGetIntegerv(GL_UNPACK_LSB_FIRST, props.getLastUnpackLsbFirst());
        glGetIntegerv(GL_UNPACK_ALIGNMENT, props.getLastUnpackAlignment());
        glGetIntegerv(GL_UNPACK_ROW_LENGTH, props.getLastUnpackRowLength());
        glGetIntegerv(GL_UNPACK_SKIP_PIXELS, props.getLastUnpackSkipPixels());
        glGetIntegerv(GL_UNPACK_SKIP_ROWS, props.getLastUnpackSkipRows());

        if (glVersion >= 120) {
            glGetIntegerv(GL_PACK_IMAGE_HEIGHT, props.getLastPackImageHeight());
            glGetIntegerv(GL_PACK_SKIP_IMAGES, props.getLastPackSkipImages());
            glGetIntegerv(GL_UNPACK_IMAGE_HEIGHT, props.getLastUnpackImageHeight());
            glGetIntegerv(GL_UNPACK_SKIP_IMAGES, props.getLastUnpackSkipImages());
        }

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
        glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
        glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);

        return this;
    }

    /**
     * Restores the state that was saved with push().
     * <p>
     * This code was inspired by [imgui-java](https://github.com/SpaiR/imgui-java/blob/2a605f0d8500f27e13fa1d2b4cf8cadd822789f4/imgui-lwjgl3/src/main/java/imgui/gl3/ImGuiImplGl3.java#L500-L532)
     * and modified to fit the project's codebase.
     *
     * @see #push()
     */
    public State pop() {
        glUseProgram(props.getLastProgram()[0]);
        glBindTexture(GL_TEXTURE_2D, props.getLastTexture()[0]);
        if (glVersion >= 330 || GL.getCapabilities().GL_ARB_sampler_objects) {
            glBindSampler(0, props.getLastSampler()[0]);
        }
        glActiveTexture(props.getLastActiveTexture()[0]);
        glBindVertexArray(props.getLastVertexArrayObject()[0]);
        glBindBuffer(GL_ARRAY_BUFFER, props.getLastArrayBuffer()[0]);
        glBlendEquationSeparate(props.getLastBlendEquationRgb()[0], props.getLastBlendEquationAlpha()[0]);
        glBlendFuncSeparate(
                props.getLastBlendSrcRgb()[0],
                props.getLastBlendDstRgb()[0],
                props.getLastBlendSrcAlpha()[0],
                props.getLastBlendDstAlpha()[0]
        );
        if (props.isLastEnableBlend()) glEnable(GL_BLEND);
        else glDisable(GL_BLEND);
        if (props.isLastEnableCullFace()) glEnable(GL_CULL_FACE);
        else glDisable(GL_CULL_FACE);
        if (props.isLastEnableDepthTest()) glEnable(GL_DEPTH_TEST);
        else glDisable(GL_DEPTH_TEST);
        if (props.isLastEnableStencilTest()) glEnable(GL_STENCIL_TEST);
        else glDisable(GL_STENCIL_TEST);
        if (props.isLastEnableScissorTest()) glEnable(GL_SCISSOR_TEST);
        else glDisable(GL_SCISSOR_TEST);
        if (glVersion >= 310) {
            if (props.isLastEnablePrimitiveRestart()) glEnable(GL_PRIMITIVE_RESTART);
            else glDisable(GL_PRIMITIVE_RESTART);
        }
        if (glVersion >= 200) {
            glPolygonMode(GL_FRONT_AND_BACK, props.getLastPolygonMode()[0]);
        }
        glViewport(props.getLastViewport()[0], props.getLastViewport()[1], props.getLastViewport()[2], props.getLastViewport()[3]);
        glScissor(
                props.getLastScissorBox()[0],
                props.getLastScissorBox()[1],
                props.getLastScissorBox()[2],
                props.getLastScissorBox()[3]
        );

        glPixelStorei(GL_PACK_SWAP_BYTES, props.getLastPackSwapBytes()[0]);
        glPixelStorei(GL_PACK_LSB_FIRST, props.getLastPackLsbFirst()[0]);
        glPixelStorei(GL_PACK_ROW_LENGTH, props.getLastPackRowLength()[0]);
        glPixelStorei(GL_PACK_SKIP_PIXELS, props.getLastPackSkipPixels()[0]);
        glPixelStorei(GL_PACK_SKIP_ROWS, props.getLastPackSkipRows()[0]);
        glPixelStorei(GL_PACK_ALIGNMENT, props.getLastPackAlignment()[0]);

        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, props.getLastPixelUnpackBufferBinding()[0]);
        glPixelStorei(GL_UNPACK_SWAP_BYTES, props.getLastUnpackSwapBytes()[0]);
        glPixelStorei(GL_UNPACK_LSB_FIRST, props.getLastUnpackLsbFirst()[0]);
        glPixelStorei(GL_UNPACK_ALIGNMENT, props.getLastUnpackAlignment()[0]);
        glPixelStorei(GL_UNPACK_ROW_LENGTH, props.getLastUnpackRowLength()[0]);
        glPixelStorei(GL_UNPACK_SKIP_PIXELS, props.getLastUnpackSkipPixels()[0]);
        glPixelStorei(GL_UNPACK_SKIP_ROWS, props.getLastUnpackSkipRows()[0]);

        if (glVersion >= 120) {
            glPixelStorei(GL_PACK_IMAGE_HEIGHT, props.getLastPackImageHeight()[0]);
            glPixelStorei(GL_PACK_SKIP_IMAGES, props.getLastPackSkipImages()[0]);
            glPixelStorei(GL_UNPACK_IMAGE_HEIGHT, props.getLastUnpackImageHeight()[0]);
            glPixelStorei(GL_UNPACK_SKIP_IMAGES, props.getLastUnpackSkipImages()[0]);
        }

        // This state is not restored in the original imgui-java project but is included to address bugs encountered when drawing with Skija.
        glDepthMask(props.isLastDepthMask()); // This is a workaround for a bug where the text renderer of Minecraft would not render text properly (flickering text). This also fixes the issue that resizing the window would cause the buttons and more to disappear.

        return this;
    }
}
