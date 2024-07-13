package sol.awakeapi.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sol.awakeapi.interfaces.IInGameHud;

import java.util.ArrayList;
import java.util.List;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin implements IInGameHud {

    private int tickDuration;
    private String hudMessage;

    @Shadow
    private MinecraftClient client;

    @Shadow
    private int scaledWidth;

    @Shadow
    private int scaledHeight;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        tickDuration--;
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(DrawContext context, float tickDelta, CallbackInfo info) {
        if (!this.client.options.hudHidden && this.hudMessage != null && this.tickDuration > 0) {
            MatrixStack matrices = context.getMatrices();

            this.client.getProfiler().push("overlayMessage");

            float h = (float)this.tickDuration - tickDelta;
            int l = (int)(h * 255.0f / 20.0f);
            if (l > 255) {
                l = 255;
            }
            if (l > 8) {
                matrices.push();
                int safeX = this.scaledWidth / 2;
                int safeY = this.scaledHeight - 68;

                TextRenderer textRenderer = this.client.textRenderer;
                int color = 0xFFFFFF;
                int y = safeY;

                // Split the message into lines
                List<String> lines = wrapText(hudMessage, scaledWidth - 40, textRenderer); // adjusted width for safe area

                // Render each line
                for (String line : lines) {
                    textRenderer.draw(line, (float)(safeX - textRenderer.getWidth(line) / 2), (float)y, color | (l << 24 & 0xFF000000), true, matrices.peek().getPositionMatrix(), this.client.getBufferBuilders().getEntityVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 15728880);
//                    textRenderer.drawWithShadow(matrices, line, (float)(safeX - textRenderer.getWidth(line) / 2), (float)y, color | (l << 24 & 0xFF000000));
                    y += textRenderer.fontHeight;
                }

                matrices.pop();
            }
            this.client.getProfiler().pop();
        }
    }

    @SuppressWarnings("UnreachableCode")
    public void displayOverlayMessage(String message, int duration) {
        ((InGameHud)(Object)this).setOverlayMessage(null, false);
        ((InGameHud)(Object)this).setCanShowChatDisabledScreen(false);
        this.hudMessage = message;
        this.tickDuration = duration;
    }

    private List<String> wrapText(String text, int maxWidth, TextRenderer textRenderer) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String potentialLine = currentLine.isEmpty() ? word : currentLine + " " + word;

            if (textRenderer.getWidth(potentialLine) <= maxWidth) {
                currentLine.append(currentLine.isEmpty() ? word : " " + word);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
}