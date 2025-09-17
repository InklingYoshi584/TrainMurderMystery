package dev.doctor4t.trainmurdermystery.mixin.client.items;

import dev.doctor4t.trainmurdermystery.client.TrainMurderMysteryClient;
import dev.doctor4t.trainmurdermystery.index.TrainMurderMysteryItems;
import dev.doctor4t.trainmurdermystery.util.MatrixParticleManager;
import dev.doctor4t.trainmurdermystery.util.MatrixUtils;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V",
            shift = At.Shift.AFTER))
    private void tmm$itemVFX(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (renderMode.isFirstPerson()) {
            TrainMurderMysteryClient.handParticleManager.render(matrices, vertexConsumers, light);
        }

        if (entity instanceof PlayerEntity playerEntity && stack.isOf(TrainMurderMysteryItems.REVOLVER)) {
            if (playerEntity.getUuid() != MinecraftClient.getInstance().player.getUuid()) {
                MatrixParticleManager.setMuzzlePosForPlayer(playerEntity, MatrixUtils.matrixToVec(matrices));
            } else if (!renderMode.isFirstPerson()) {
                MatrixParticleManager.setMuzzlePosForPlayer(playerEntity, MatrixUtils.matrixToVec(matrices));
            }
        }
    }
}
