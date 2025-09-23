package dev.doctor4t.trainmurdermystery.client.gui;

import dev.doctor4t.trainmurdermystery.cca.TMMComponents;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class RoleNameRenderer {
    private static float nametagAlpha = 0f;
    private static TrainRole targetRole = TrainRole.BYSTANDER;
    private static Text nametag = Text.empty();

    public static void renderHud(TextRenderer renderer, @NotNull ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter) {
        var component = TMMComponents.GAME.get(player.getWorld());
        var result = ProjectileUtil.getCollision(player, entity -> entity instanceof PlayerEntity, 2f);
        if (result instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof PlayerEntity target) {
            nametagAlpha = MathHelper.lerp(tickCounter.getTickDelta(true) / 4, nametagAlpha, 1f);
            nametag = target.getDisplayName();
            if (component.isDetective(target)) {
                targetRole = TrainRole.DETECTIVE;
            } else if (component.isHitman(target)) {
                targetRole = TrainRole.HITMAN;
            } else {
                targetRole = TrainRole.BYSTANDER;
            }
        } else {
            nametagAlpha = MathHelper.lerp(tickCounter.getTickDelta(true) / 4, nametagAlpha, 0f);
        }
        if (nametagAlpha > 0.05f) {
            context.getMatrices().push();
            context.getMatrices().translate(context.getScaledWindowWidth() / 2f, context.getScaledWindowHeight() / 2f + 6, 0);
            context.getMatrices().scale(0.6f, 0.6f, 1f);
            var nameWidth = renderer.getWidth(nametag);
            context.drawTextWithShadow(renderer, nametag, -nameWidth / 2, 16, MathHelper.packRgb(1f, 1f, 1f) | ((int) (nametagAlpha * 255) << 24));
            if (component.isRunning()) {
                var playerRole = TrainRole.BYSTANDER;
                if (component.isDetective(player)) {
                    playerRole = TrainRole.DETECTIVE;
                } else if (component.isHitman(player)) {
                    playerRole = TrainRole.HITMAN;
                }
                if (playerRole == TrainRole.HITMAN && targetRole == TrainRole.HITMAN) {
                    context.getMatrices().translate(0, 20 + renderer.fontHeight, 0);
                    var roleText = Text.literal("HITMAN COHORT");
                    var roleWidth = renderer.getWidth(roleText);
                    context.drawTextWithShadow(renderer, roleText, -roleWidth / 2, 0, MathHelper.packRgb(1f, 0f, 0f) | ((int) (nametagAlpha * 255) << 24));
                }
            }
            context.getMatrices().pop();
        }
    }

    private enum TrainRole {
        DETECTIVE,
        HITMAN,
        BYSTANDER
    }
}