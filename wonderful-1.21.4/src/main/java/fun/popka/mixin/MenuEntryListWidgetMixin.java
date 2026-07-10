package fun.popka.mixin;

import fun.popka.api.utils.color.ColorUtils;
import fun.popka.api.utils.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntryListWidget.class)
public abstract class MenuEntryListWidgetMixin {

    private static final int PANEL_COLOR = ColorUtils.rgba(0, 0, 0, 160);
    private static final int BORDER_COLOR = ColorUtils.rgba(40, 40, 40, 200);
    private static final float CORNER_RADIUS = 6f;

    @Inject(method = "renderWidget", at = @At("HEAD"))
    private void popka$renderWidget(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world != null) {
            return;
        }

        ClickableWidget self = (ClickableWidget) (Object) this;
        if (!self.visible) {
            return;
        }

        int x = self.getX();
        int y = self.getY();
        int w = self.getWidth();
        int h = self.getHeight();

        RenderUtils.drawRoundedRect(context.getMatrices(), x - 1, y - 1, w + 2, h + 2, CORNER_RADIUS + 1, BORDER_COLOR);
        RenderUtils.drawRoundedRect(context.getMatrices(), x, y, w, h, CORNER_RADIUS, PANEL_COLOR);
    }
}
