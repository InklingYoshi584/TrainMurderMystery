package dev.doctor4t.trainmurdermystery.client.gui;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.cca.TMMComponents;
import dev.doctor4t.trainmurdermystery.cca.WorldGameComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

public class MoodRenderer {
    public static final Identifier ARROW_UP = TMM.id("hud/arrow_up");
    public static final Identifier ARROW_DOWN = TMM.id("hud/arrow_down");
    public static final Identifier MOOD_HAPPY = TMM.id("hud/mood_happy");
    public static final Identifier MOOD_MID = TMM.id("hud/mood_mid");
    public static final Identifier MOOD_DEPRESSIVE = TMM.id("hud/mood_depressive");
    private static String previousPreferenceText = "";
    private static float preferenceTextAlpha = 0f;
    public static float arrowProgress = 1f;

    @Environment(EnvType.CLIENT)
    public static void renderHud(PlayerEntity player, TextRenderer renderer, DrawContext context, RenderTickCounter tickCounter) {
        if (!TMMComponents.GAME.get(player.getWorld()).isRunning()) return;
        var component = PlayerMoodComponent.KEY.get(player);
        if (!Objects.equals(previousPreferenceText, component.preferenceText)) {
            preferenceTextAlpha = MathHelper.lerp(tickCounter.getTickDelta(true) / 4, preferenceTextAlpha, 0f);
            if (preferenceTextAlpha <= 0.01f) previousPreferenceText = component.preferenceText;
        } else {
            preferenceTextAlpha = MathHelper.lerp(tickCounter.getTickDelta(true) / 4, preferenceTextAlpha, 1f);
        }
        if (previousPreferenceText.isEmpty()) return;
        var textWidth = renderer.getWidth(previousPreferenceText);
        context.getMatrices().push();
        context.getMatrices().translate(-(24 + textWidth) * (1f - preferenceTextAlpha), 0, 0);
        var mood = MOOD_HAPPY;
        if (component.mood < 0.2f) {
            mood = MOOD_DEPRESSIVE;
        } else if (component.mood < 0.55f) {
            mood = MOOD_MID;
        }
        context.drawGuiTexture(mood, 5, 6, 14, 17);
        arrowProgress = MathHelper.lerp(tickCounter.getTickDelta(true) / 16, arrowProgress, 1f);
        if (arrowProgress < 0.99f) {
            var arrow = component.fulfilled ? ARROW_UP : ARROW_DOWN;
            context.getMatrices().push();
            context.getMatrices().translate(0, component.fulfilled ? 4 - arrowProgress * 4 : arrowProgress * 4, 0);
            context.drawSprite(7, 6, 0, 10, 13, context.guiAtlasManager.getSprite(arrow), 1f, 1f, 1f, (float) Math.sin(arrowProgress * Math.PI));
            context.getMatrices().pop();
        }
        context.drawTextWithShadow(renderer, previousPreferenceText, 22, 8, MathHelper.packRgb(1f, 1f, 1f) | ((int) (preferenceTextAlpha * 255) << 24));
        context.getMatrices().pop();
        context.getMatrices().push();
        context.getMatrices().translate(26, 10 + renderer.fontHeight, 0);
        context.getMatrices().translate(-(24 + textWidth) * (1f - preferenceTextAlpha), 0, 0);
        context.getMatrices().scale((textWidth - 8) * component.mood, 1, 1);
        context.fill(0, 0, 1, 1, MathHelper.hsvToRgb(component.mood / 3.0F, 1.0F, 1.0F) | ((int) (preferenceTextAlpha * 255) << 24));
        context.getMatrices().pop();
    }
}
