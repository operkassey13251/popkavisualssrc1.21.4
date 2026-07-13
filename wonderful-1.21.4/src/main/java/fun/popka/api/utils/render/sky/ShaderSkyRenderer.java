package fun.popka.api.utils.render.sky;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import fun.popka.Popka;
import fun.popka.api.QClient;
import fun.popka.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.popka.api.utils.color.ColorUtils;
import fun.popka.api.utils.render.ShaderUtils;
import fun.popka.visuals.modules.impl.render.ShaderSky;

public class ShaderSkyRenderer implements QClient {
    private static final float EPSILON = 0.001f;
    private static ShaderSkyRenderer instance;

    private Framebuffer maskBuffer;
    private Framebuffer depthCopyBuffer;
    private int width = -1;
    private int height = -1;
    private int configuredDepthTex = -1;

    public static ShaderSkyRenderer getInstance() {
        if (instance == null) instance = new ShaderSkyRenderer();
        return instance;
    }

    public void invalidateState() {
        configuredDepthTex = -1;
    }

    public void renderSky() {
        ShaderSky module = getModule();
        if (!isEffectEnabled(module)) return;
        if (mc.world == null) return;

        ensureBuffers();
        if (maskBuffer == null || depthCopyBuffer == null) return;

        Framebuffer mainBuffer = mc.getFramebuffer();
        if (mainBuffer.getDepthAttachment() == 0) return;

        copyDepthFromMain(mainBuffer);

        int depthTex = depthCopyBuffer.getDepthAttachment();
        if (depthTex == 0) return;

        if (depthTex != configuredDepthTex) {
            configureDepthTexture(depthTex);
            configuredDepthTex = depthTex;
        }

        ShaderProgram maskShader = mc.getShaderLoader().getOrCreateProgram(ShaderUtils.shaderSkyMask);
        if (maskShader == null) return;

        Matrix4f savedProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        ProjectionType savedProjectionType = RenderSystem.getProjectionType();
        float sw = Math.max(mc.getWindow().getScaledWidth(), 1);
        float sh = Math.max(mc.getWindow().getScaledHeight(), 1);
        Matrix4f ortho = new Matrix4f().setOrtho(0.0f, sw, sh, 0.0f, -1000.0f, 1000.0f);
        RenderSystem.setProjectionMatrix(ortho, ProjectionType.ORTHOGRAPHIC);
        var mvStack = RenderSystem.getModelViewStack();
        mvStack.pushMatrix();
        mvStack.identity();

        try {
            maskBuffer.setClearColor(0f, 0f, 0f, 0f);
            maskBuffer.clear();
            maskBuffer.beginWrite(false);
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.setShader(ShaderUtils.shaderSkyMask);
            RenderSystem.setShaderTexture(0, depthTex);
            drawFullscreenQuad();

            boolean isCosmic = module.mode.is("Космос");
            var overlayKey = isCosmic ? ShaderUtils.skyCosmic : ShaderUtils.blockOverlay;
            ShaderProgram overlayShader = mc.getShaderLoader().getOrCreateProgram(overlayKey);
            if (overlayShader != null) {
                int color1 = getThemeColor1();
                int color2 = getThemeColor2();

                mainBuffer.beginWrite(false);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);

                RenderSystem.setShader(overlayKey);
                RenderSystem.setShaderTexture(0, maskBuffer.getColorAttachment());

                setUniform(overlayShader, "color", ColorUtils.redf(color1), ColorUtils.greenf(color1), ColorUtils.bluef(color1));
                setUniform(overlayShader, "color2", ColorUtils.redf(color2), ColorUtils.greenf(color2), ColorUtils.bluef(color2));
                setUniform(overlayShader, "time", (System.currentTimeMillis() % 100000L) / 1000.0f);
                setUniform(overlayShader, "alpha", module.alpha.get());

                float m11 = savedProjection.m11();
                float fovDeg = m11 > 0.001f ? (float) Math.toDegrees(2.0 * Math.atan(1.0 / m11)) : 70.0f;
                int fbW = mc.getWindow().getFramebufferWidth();
                int fbH = mc.getWindow().getFramebufferHeight();
                float aspectVal = fbH > 0 ? (float) fbW / (float) fbH : 1.0f;
                float[] basis = computeCameraBasis();

                if (isCosmic) {
                    setUniform(overlayShader, "fov", fovDeg);
                    setUniform(overlayShader, "aspect", aspectVal);
                    setUniform(overlayShader, "starDensity", module.starDensity.get());
                    setUniform(overlayShader, "nebulaIntensity", module.nebulaIntensity.get());
                    setUniform(overlayShader, "twinkleSpeed", module.twinkleSpeed.get());

                    setUniform(overlayShader, "cameraRight", basis[0], basis[1], basis[2]);
                    setUniform(overlayShader, "cameraUp", basis[3], basis[4], basis[5]);
                    setUniform(overlayShader, "cameraForward", basis[6], basis[7], basis[8]);
                } else {
                    setUniform(overlayShader, "texelSize",
                            1.0f / Math.max(1, mc.getWindow().getFramebufferWidth()),
                            1.0f / Math.max(1, mc.getWindow().getFramebufferHeight()));
                    setUniform(overlayShader, "speed", module.waveSpeed.get());
                    setUniform(overlayShader, "scale", module.waveScale.get());
                    setUniform(overlayShader, "outline", module.outline.get());
                    setUniform(overlayShader, "glow", module.glow.get());
                    setUniform(overlayShader, "fill", module.fill.get());
                    setUniform(overlayShader, "outlineOnly", 0.0f);

                    setUniform(overlayShader, "worldSpace", 1.0f);
                    setUniform(overlayShader, "fov", fovDeg);
                    setUniform(overlayShader, "aspect", aspectVal);
                    setUniform(overlayShader, "cameraRight", basis[0], basis[1], basis[2]);
                    setUniform(overlayShader, "cameraUp", basis[3], basis[4], basis[5]);
                    setUniform(overlayShader, "cameraForward", basis[6], basis[7], basis[8]);
                }
                drawFullscreenQuad();
            }

            restoreState();
        } finally {
            mvStack.popMatrix();
            RenderSystem.setProjectionMatrix(savedProjection, savedProjectionType);
        }
    }

    private void copyDepthFromMain(Framebuffer mainBuffer) {
        int readFbo = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int drawFbo = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int w = mainBuffer.textureWidth;
        int h = mainBuffer.textureHeight;

        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainBuffer.fbo);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, depthCopyBuffer.fbo);
        GL30.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFbo);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFbo);
    }

    private void configureDepthTexture(int depthTex) {
        RenderSystem.bindTexture(depthTex);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, GL11.GL_NONE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        RenderSystem.bindTexture(0);
    }

    private void ensureBuffers() {
        int w = mc.getWindow().getFramebufferWidth();
        int h = mc.getWindow().getFramebufferHeight();
        if (w == width && h == height && maskBuffer != null && depthCopyBuffer != null) return;

        if (maskBuffer != null) maskBuffer.delete();
        if (depthCopyBuffer != null) depthCopyBuffer.delete();
        maskBuffer = new SimpleFramebuffer(w, h, false);
        depthCopyBuffer = new SimpleFramebuffer(w, h, true);
        width = w;
        height = h;
        configuredDepthTex = -1;
    }

    private void restoreState() {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, 0);
        mc.getFramebuffer().beginWrite(true);
    }

    private boolean isEffectEnabled(ShaderSky module) {
        if (module == null || !module.isEnable()) return false;
        if (module.mode.is("Космос")) {
            return module.alpha.get() > EPSILON;
        }
        boolean hasGlow = module.glow.get() > EPSILON;
        boolean hasFill = module.fill.get() > EPSILON && module.alpha.get() > EPSILON;
        return hasGlow || hasFill;
    }

    private int getThemeColor1() {
        if (Popka.INSTANCE == null || Popka.INSTANCE.themeStorage == null || Popka.INSTANCE.themeStorage.getThemes() == null) {
            return ColorUtils.getThemeColor(0);
        }
        var theme = Popka.INSTANCE.themeStorage.getThemes().getTheme();
        if (theme == null) return ColorUtils.getThemeColor(0);
        if (theme.getName().equals("Rainbow")) {
            return ColorUtils.getThemeColor(0);
        }
        return theme.color != null && theme.color.length > 0 ? theme.color[0] : ColorUtils.getThemeColor(0);
    }

    private int getThemeColor2() {
        if (Popka.INSTANCE == null || Popka.INSTANCE.themeStorage == null || Popka.INSTANCE.themeStorage.getThemes() == null) {
            return ColorUtils.getThemeColor(180);
        }
        var theme = Popka.INSTANCE.themeStorage.getThemes().getTheme();
        if (theme == null) return ColorUtils.getThemeColor(180);
        if (theme.getName().equals("Rainbow")) {
            return ColorUtils.getThemeColor(180);
        }
        return theme.color != null && theme.color.length > 0 ? theme.color[0] : ColorUtils.getThemeColor(180);
    }

    private ShaderSky getModule() {
        if (Popka.INSTANCE == null || ModuleClass.INSTANCE == null) return null;
        return ModuleClass.INSTANCE.shaderSky;
    }

    private void setUniform(ShaderProgram shader, String name, float v) {
        GlUniform u = shader.getUniform(name);
        if (u != null) u.set(v);
    }

    private float[] computeCameraBasis() {
        if (mc.gameRenderer == null || mc.gameRenderer.getCamera() == null) {
            return new float[]{1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f};
        }
        float yaw = (float) Math.toRadians(mc.gameRenderer.getCamera().getYaw());
        float pitch = (float) Math.toRadians(mc.gameRenderer.getCamera().getPitch());

        float sy = (float) Math.sin(yaw);
        float cy = (float) Math.cos(yaw);
        float sp = (float) Math.sin(pitch);
        float cp = (float) Math.cos(pitch);

        float fx = -sy * cp;
        float fy = -sp;
        float fz =  cy * cp;

        float rx = -fz;
        float ry = 0.0f;
        float rz =  fx;
        float rLen = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
        if (rLen > 1e-6f) {
            rx /= rLen; ry /= rLen; rz /= rLen;
        }

        float ux = ry * fz - rz * fy;
        float uy = rz * fx - rx * fz;
        float uz = rx * fy - ry * fx;

        return new float[]{rx, ry, rz, ux, uy, uz, fx, fy, fz};
    }

    private void setUniform(ShaderProgram shader, String name, float x, float y) {
        GlUniform u = shader.getUniform(name);
        if (u != null) u.set(x, y);
    }

    private void setUniform(ShaderProgram shader, String name, float x, float y, float z) {
        GlUniform u = shader.getUniform(name);
        if (u != null) u.set(x, y, z);
    }

    private void drawFullscreenQuad() {
        float sw = Math.max(mc.getWindow().getScaledWidth(), 1);
        float sh = Math.max(mc.getWindow().getScaledHeight(), 1);
        BufferBuilder b = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        b.vertex(0, 0, 0).texture(0, 1).color(1f, 1f, 1f, 1f);
        b.vertex(0, sh, 0).texture(0, 0).color(1f, 1f, 1f, 1f);
        b.vertex(sw, sh, 0).texture(1, 0).color(1f, 1f, 1f, 1f);
        b.vertex(sw, 0, 0).texture(1, 1).color(1f, 1f, 1f, 1f);
        BufferRenderer.drawWithGlobalProgram(b.end());
    }
}
