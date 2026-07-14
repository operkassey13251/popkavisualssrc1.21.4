package fun.popka.visuals.modules.impl.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import fun.popka.api.events.EventLink;
import fun.popka.api.events.implement.Event3DRender;
import fun.popka.api.events.implement.EventAttackEntity;
import fun.popka.api.events.implement.EventUpdate;
import fun.popka.api.utils.color.ColorUtils;
import fun.popka.visuals.modules.Module;
import fun.popka.visuals.modules.settings.implement.BooleanSetting;
import fun.popka.visuals.modules.settings.implement.FloatSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Particles extends Module {

    public static Particles INSTANCE = new Particles();

    private static final Identifier GLOW_TEXTURE = Identifier.of("popka", "textures/particle/bloom.png");
    private static final float MAX_RENDER_DISTANCE_SQ = 900.0f;
    private static final float[] GLOW_SCALES = {9.0f, 5.0f, 2.2f};
    private static final float[] GLOW_ALPHA = {0.08f, 0.20f, 0.60f};

    private final FloatSetting count = new FloatSetting("Количество", 40.0f, 5.0f, 200.0f, 1.0f);
    private final FloatSetting glow = new FloatSetting("Свечение", 2.0f, 0.1f, 5.0f, 0.1f);
    private final FloatSetting size = new FloatSetting("Размер", 0.12f, 0.04f, 0.5f, 0.01f);
    private final FloatSetting speed = new FloatSetting("Скорость", 1.0f, 0.1f, 3.0f, 0.1f);
    private final FloatSetting radius = new FloatSetting("Радиус", 14.0f, 4.0f, 30.0f, 1.0f);
    private final BooleanSetting onHit = new BooleanSetting("При ударе", false);
    private final FloatSetting hitCount = new FloatSetting("Кол-во при ударе", 20.0f, 1.0f, 100.0f, 1.0f).visible(onHit::isState);
    private final BooleanSetting beerMug = new BooleanSetting("Кружка пива", false).visible(onHit::isState);

    private final List<GlowParticle> particles = new ArrayList<>();
    private final List<GlowParticle> hitParticles = new ArrayList<>();
    private final Random random = new Random();

    public Particles() {
        super("GlowParticles", "Маленькие светящиеся частицы по миру", ModuleCategory.RENDER);
        addSettings(count, glow, size, speed, radius, onHit, hitCount, beerMug);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        particles.clear();
        hitParticles.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        particles.clear();
        hitParticles.clear();
    }

    @EventLink
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;
        updateParticles();
        updateHitParticles();
    }

    @EventLink
    public void onRender3D(Event3DRender e) {
        if (mc.player == null || mc.world == null) return;
        if (particles.isEmpty() && hitParticles.isEmpty()) return;
        renderParticles(e);
    }

    @EventLink
    public void onAttack(EventAttackEntity event) {
        if (!onHit.isState()) return;
        if (event == null || event.getTarget() == null) return;

        net.minecraft.entity.Entity target = event.getTarget();
        double cx = target.getX();
        double cy = target.getY() + target.getHeight() * 0.5;
        double cz = target.getZ();

        if (beerMug.isState()) {
            int n = Math.max(1, (int) hitCount.get() / 4);
            for (int i = 0; i < n; i++) hitParticles.add(spawnBeerMug(cx, cy, cz));
        } else {
            int n = (int) hitCount.get();
            for (int i = 0; i < n; i++) hitParticles.add(spawnAt(cx, cy, cz));
        }
    }

    private void updateParticles() {
        int target = (int) count.get();
        int currentSize = particles.size();

        if (currentSize < target) {
            int toAdd = Math.min(target - currentSize, 6);
            for (int i = 0; i < toAdd; i++) particles.add(spawn());
        } else if (currentSize > target) {
            particles.subList(target, currentSize).clear();
        }

        float spd = speed.get();
        float maxR = radius.get();
        Vec3d playerPos = mc.player.getPos();
        double maxRSq = maxR * maxR * 4.0;

        for (int i = particles.size() - 1; i >= 0; i--) {
            GlowParticle p = particles.get(i);

            p.x += p.vx * spd;
            p.y += p.vy * spd;
            p.z += p.vz * spd;

            p.vx *= 0.992f;
            p.vy = p.vy * 0.992f + p.buoyancy * 0.0002f * spd;
            p.vz *= 0.992f;

            p.life--;
            p.phase += 0.05f * spd;

            double dx = p.x - playerPos.x;
            double dy = p.y - playerPos.y;
            double dz = p.z - playerPos.z;
            double distSq = dx * dx + dy * dy + dz * dz;

            if (p.life <= 0 || distSq > maxRSq) {
                particles.remove(i);
                particles.add(spawn());
            }
        }
    }

    private void updateHitParticles() {
        if (hitParticles.isEmpty()) return;

        float spd = speed.get();

        for (int i = hitParticles.size() - 1; i >= 0; i--) {
            GlowParticle p = hitParticles.get(i);

            p.x += p.vx * spd;
            p.y += p.vy * spd;
            p.z += p.vz * spd;

            p.vx *= 0.94f;
            p.vy = p.vy * 0.94f + p.buoyancy * 0.0004f * spd;
            p.vz *= 0.94f;

            p.life--;
            p.phase += 0.08f * spd;

            if (p.beer) p.rotation += p.rotSpeed * spd;

            if (p.life <= 0) hitParticles.remove(i);
        }
    }

    private GlowParticle spawn() {
        float r = radius.get();
        double x = mc.player.getX() + (random.nextDouble() * 2 - 1) * r;
        double y = mc.player.getY() + (random.nextDouble() * 2 - 1) * r * 0.6 + 1.0;
        double z = mc.player.getZ() + (random.nextDouble() * 2 - 1) * r;

        float yaw = random.nextFloat() * 360f;
        float vel = 0.006f + random.nextFloat() * 0.014f;
        float vx = -MathHelper.sin((float) Math.toRadians(yaw)) * vel;
        float vz = MathHelper.cos((float) Math.toRadians(yaw)) * vel;
        float vy = (random.nextFloat() - 0.5f) * 0.006f;

        int life = 200 + random.nextInt(220);
        float buoyancy = random.nextBoolean() ? 1.0f : -0.4f;

        return new GlowParticle(x, y, z, vx, vy, vz, life, random.nextFloat() * (float) Math.PI * 2f, buoyancy);
    }

    private GlowParticle spawnAt(double cx, double cy, double cz) {
        float yaw = random.nextFloat() * 360f;
        float pitch = (random.nextFloat() - 0.5f) * 60f;
        float vel = 0.02f + random.nextFloat() * 0.04f;
        float vx = -MathHelper.sin((float) Math.toRadians(yaw)) * MathHelper.cos((float) Math.toRadians(pitch)) * vel;
        float vz = MathHelper.cos((float) Math.toRadians(yaw)) * MathHelper.cos((float) Math.toRadians(pitch)) * vel;
        float vy = MathHelper.sin((float) Math.toRadians(pitch)) * vel + 0.01f;

        int life = 80 + random.nextInt(80);
        float buoyancy = random.nextBoolean() ? 0.6f : -0.2f;

        return new GlowParticle(cx, cy, cz, vx, vy, vz, life, random.nextFloat() * (float) Math.PI * 2f, buoyancy);
    }

    private GlowParticle spawnBeerMug(double cx, double cy, double cz) {
        float yaw = random.nextFloat() * 360f;
        float pitch = (random.nextFloat() - 0.5f) * 50f;
        float vel = 0.03f + random.nextFloat() * 0.05f;
        float vx = -MathHelper.sin((float) Math.toRadians(yaw)) * MathHelper.cos((float) Math.toRadians(pitch)) * vel;
        float vz = MathHelper.cos((float) Math.toRadians(yaw)) * MathHelper.cos((float) Math.toRadians(pitch)) * vel;
        float vy = MathHelper.sin((float) Math.toRadians(pitch)) * vel + 0.02f;

        int life = 60 + random.nextInt(50);
        float buoyancy = -1.6f;

        GlowParticle p = new GlowParticle(cx, cy, cz, vx, vy, vz, life, 0f, buoyancy);
        p.beer = true;
        p.rotation = random.nextFloat() * 360f;
        p.rotSpeed = (random.nextFloat() - 0.5f) * 18f;
        return p;
    }

    private void renderParticles(Event3DRender e) {
        MatrixStack ms = e.getMatrices();
        Camera camera = e.getCamera();
        Vec3d cam = camera.getPos();

        float s = size.get();
        float glowPower = glow.get();

        int baseRGB = ColorUtils.getThemeColor();
        float cr = ((baseRGB >> 16) & 0xFF) / 255f;
        float cg = ((baseRGB >> 8) & 0xFF) / 255f;
        float cb = (baseRGB & 0xFF) / 255f;

        List<GlowParticle> visible = new ArrayList<>();
        List<GlowParticle> visibleBeer = new ArrayList<>();
        for (int i = 0, sz = particles.size(); i < sz; i++) {
            GlowParticle p = particles.get(i);
            double dx = p.x - cam.x;
            double dy = p.y - cam.y;
            double dz = p.z - cam.z;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > MAX_RENDER_DISTANCE_SQ) continue;

            float a = getAlpha(p);
            if (a < 0.01f) continue;
            p.renderAlpha = a;
            visible.add(p);
        }

        for (int i = 0, sz = hitParticles.size(); i < sz; i++) {
            GlowParticle p = hitParticles.get(i);
            double dx = p.x - cam.x;
            double dy = p.y - cam.y;
            double dz = p.z - cam.z;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > MAX_RENDER_DISTANCE_SQ) continue;

            float a = getAlpha(p);
            if (a < 0.01f) continue;
            p.renderAlpha = a;
            if (p.beer) visibleBeer.add(p);
            else visible.add(p);
        }

        if (visible.isEmpty() && visibleBeer.isEmpty()) return;

        if (!visible.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture(0, GLOW_TEXTURE);

            BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            for (int i = 0, sz = visible.size(); i < sz; i++) {
                GlowParticle p = visible.get(i);
                float pulse = 0.85f + 0.15f * MathHelper.sin(p.phase);
                float alpha = p.renderAlpha * pulse;

                ms.push();
                ms.translate(p.x - cam.x, p.y - cam.y, p.z - cam.z);
                ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

                Matrix4f matrix = ms.peek().getPositionMatrix();

                for (int g = 0; g < 3; g++) {
                    float scale = s * GLOW_SCALES[g] * glowPower;
                    float a = Math.min(1.0f, alpha * GLOW_ALPHA[g] * glowPower);
                    float hs = scale * 0.5f;

                    builder.vertex(matrix, -hs, hs, 0).texture(0f, 1f).color(cr, cg, cb, a);
                    builder.vertex(matrix, hs, hs, 0).texture(1f, 1f).color(cr, cg, cb, a);
                    builder.vertex(matrix, hs, -hs, 0).texture(1f, 0f).color(cr, cg, cb, a);
                    builder.vertex(matrix, -hs, -hs, 0).texture(0f, 0f).color(cr, cg, cb, a);
                }
                ms.pop();
            }

            BufferRenderer.drawWithGlobalProgram(builder.end());

            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }

        if (!visibleBeer.isEmpty()) {
            renderBeerMugs(ms, camera, cam, visibleBeer, s);
        }
    }

    private void renderBeerMugs(MatrixStack ms, Camera camera, Vec3d cam, List<GlowParticle> visibleBeer, float s) {
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        float mugScale = s * 5.0f;
        float halfW = mugScale * 0.6f;
        float halfH = mugScale * 0.8f;
        float foamH = halfH * 0.35f;
        float bodyTop = halfH - foamH;
        float handleW = halfW * 0.35f;
        float handleH = halfH * 0.55f;

        for (int i = 0, sz = visibleBeer.size(); i < sz; i++) {
            GlowParticle p = visibleBeer.get(i);
            float alpha = p.renderAlpha;

            ms.push();
            ms.translate(p.x - cam.x, p.y - cam.y, p.z - cam.z);
            ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
            ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(p.rotation));

            Matrix4f m = ms.peek().getPositionMatrix();

            float bodyA = alpha * 0.85f;
            builder.vertex(m, -halfW, -halfH, 0).color(0.95f, 0.60f, 0.15f, bodyA);
            builder.vertex(m, halfW, -halfH, 0).color(0.95f, 0.60f, 0.15f, bodyA);
            builder.vertex(m, halfW, bodyTop, 0).color(0.95f, 0.60f, 0.15f, bodyA);
            builder.vertex(m, -halfW, bodyTop, 0).color(0.95f, 0.60f, 0.15f, bodyA);

            float foamA = alpha;
            builder.vertex(m, -halfW, bodyTop, 0).color(1.0f, 0.97f, 0.85f, foamA);
            builder.vertex(m, halfW, bodyTop, 0).color(1.0f, 0.97f, 0.85f, foamA);
            builder.vertex(m, halfW, halfH, 0).color(1.0f, 0.97f, 0.85f, foamA);
            builder.vertex(m, -halfW, halfH, 0).color(1.0f, 0.97f, 0.85f, foamA);

            float handleA = alpha * 0.9f;
            builder.vertex(m, halfW, -handleH, 0).color(0.75f, 0.48f, 0.12f, handleA);
            builder.vertex(m, halfW + handleW, -handleH, 0).color(0.75f, 0.48f, 0.12f, handleA);
            builder.vertex(m, halfW + handleW, handleH, 0).color(0.75f, 0.48f, 0.12f, handleA);
            builder.vertex(m, halfW, handleH, 0).color(0.75f, 0.48f, 0.12f, handleA);

            ms.pop();
        }

        BufferRenderer.drawWithGlobalProgram(builder.end());

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private float getAlpha(GlowParticle p) {
        float lifePct = MathHelper.clamp(p.life / (float) p.maxLife, 0f, 1f);
        float fadeIn = Math.min(1f, (p.maxLife - p.life) / 25f);
        return lifePct * fadeIn;
    }

    private static class GlowParticle {
        double x, y, z;
        float vx, vy, vz;
        int life, maxLife;
        float phase;
        float buoyancy;
        float renderAlpha;
        boolean beer;
        float rotation;
        float rotSpeed;

        GlowParticle(double x, double y, double z, float vx, float vy, float vz, int life, float phase, float buoyancy) {
            this.x = x; this.y = y; this.z = z;
            this.vx = vx; this.vy = vy; this.vz = vz;
            this.life = this.maxLife = life;
            this.phase = phase;
            this.buoyancy = buoyancy;
        }
    }
}
