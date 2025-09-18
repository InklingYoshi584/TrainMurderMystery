package dev.doctor4t.trainmurdermystery.item;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.block.SmallDoorBlock;
import dev.doctor4t.trainmurdermystery.block.UnblastableDoorBlock;
import dev.doctor4t.trainmurdermystery.block_entity.SmallDoorBlockEntity;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.particle.HandParticle;
import dev.doctor4t.trainmurdermystery.client.render.TMMRenderLayers;
import dev.doctor4t.trainmurdermystery.game.TMMGameLoop;
import dev.doctor4t.trainmurdermystery.index.TMMDataComponentTypes;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import dev.doctor4t.trainmurdermystery.util.ShootMuzzleS2CPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.*;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class RevolverItem extends Item {
    public RevolverItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return shoot(world, user, hand);
    }

    private @NotNull TypedActionResult<ItemStack> shoot(World world, PlayerEntity user, Hand hand) {
        ItemStack stackInHand = user.getStackInHand(hand);
        Integer bullets = stackInHand.get(TMMDataComponentTypes.BULLETS);

        if (bullets == null) {
            bullets = 6;
        }

        if (!world.isClient) {
            world.playSound(null, user.getX(), user.getEyeY(), user.getZ(), TMMSounds.ITEM_REVOLVER_CLICK, SoundCategory.PLAYERS, 0.5f, 1f + world.random.nextFloat() * .1f - .05f);
            if (bullets > 0) {
                if (user instanceof ServerPlayerEntity shooter) {
                    for (ServerPlayerEntity tracking : PlayerLookup.tracking(shooter)) {
                        ServerPlayNetworking.send(tracking, new ShootMuzzleS2CPayload(shooter.getUuidAsString()));
                    }
                    ServerPlayNetworking.send(shooter, new ShootMuzzleS2CPayload(shooter.getUuidAsString()));
                }

                world.playSound(null, user.getX(), user.getEyeY(), user.getZ(), TMMSounds.ITEM_REVOLVER_SHOOT, SoundCategory.PLAYERS, 5f, 1f + world.random.nextFloat() * .1f - .05f);
                user.getItemCooldownManager().set(this, 20);
                if (!user.isCreative()) stackInHand.set(TMMDataComponentTypes.BULLETS, bullets-1);

                HitResult collision = ProjectileUtil.getCollision(user, entity -> entity.isAlive() && entity.isAttackable(), 20f);
                if (collision instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof PlayerEntity killedPlayer && TMMGameLoop.isPlayerAliveAndSurvival(killedPlayer)) {
                    TMMGameLoop.killPlayer(killedPlayer, true);
                }
                return TypedActionResult.consume(user.getStackInHand(hand));
            } else {
                return TypedActionResult.fail(user.getStackInHand(hand));
            }
        } else {
            if (bullets > 0) {
                user.setPitch(user.getPitch() - 4);

                HandParticle handParticle = new HandParticle()
                        .setTexture(TMM.id("textures/particle/gunshot.png"))
                        .setPos(0.1f, 0.275f, -0.2f)
                        .setMaxAge(8)
                        .setSize(0.5f)
                        .setVelocity(0f, 0f, 0f)
                        .setLight(15, 15)
                        .setAlpha(0.1f, 1f, 1f, 1f, 0.1f)
                        .setRenderLayer(TMMRenderLayers::additive);

                TMMClient.handParticleManager.spawn(handParticle);

                return TypedActionResult.consume(user.getStackInHand(hand));
            } else {
                return TypedActionResult.fail(user.getStackInHand(hand));
            }
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        TypedActionResult<ItemStack> shoot = shoot(world, player, context.getHand());

        if (shoot.getResult() == ActionResult.CONSUME && state.getBlock() instanceof SmallDoorBlock && !(state.getBlock() instanceof UnblastableDoorBlock)) {
            BlockPos lowerPos = state.get(SmallDoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.down();
            if (world.getBlockEntity(lowerPos) instanceof SmallDoorBlockEntity entity) {
                entity.blast();
            }
        }
        return shoot.getResult();
    }
}
