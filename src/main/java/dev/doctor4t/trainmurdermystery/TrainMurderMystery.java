package dev.doctor4t.trainmurdermystery;

import dev.doctor4t.trainmurdermystery.command.GiveRoomKeyCommand;
import dev.doctor4t.trainmurdermystery.command.SetTrainSpeedCommand;
import dev.doctor4t.trainmurdermystery.command.StartGameCommand;
import dev.doctor4t.trainmurdermystery.game.GameLoop;
import dev.doctor4t.trainmurdermystery.index.*;
import dev.doctor4t.trainmurdermystery.util.HandParticleManager;
import dev.doctor4t.trainmurdermystery.util.MatrixParticleManager;
import dev.doctor4t.trainmurdermystery.util.ShootMuzzleS2CPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class TrainMurderMystery implements ModInitializer {
    public static final String MOD_ID = "trainmurdermystery";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        // Registry initializers
        TMMDataComponentTypes.initialize();
        TrainMurderMysterySounds.initialize();
        TrainMurderMysteryEntities.initialize();
        TrainMurderMysteryBlocks.initialize();
        TrainMurderMysteryItems.initialize();
        TrainMurderMysteryBlockEntities.initialize();
        TrainMurderMysteryParticles.initialize();

        // Register commands
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            GiveRoomKeyCommand.register(dispatcher);
            SetTrainSpeedCommand.register(dispatcher);
            StartGameCommand.register(dispatcher);
        }));

        // Game loop tick
        ServerTickEvents.START_WORLD_TICK.register(GameLoop::tick);

        PayloadTypeRegistry.playS2C().register(ShootMuzzleS2CPayload.ID, ShootMuzzleS2CPayload.CODEC);
    }

    public static boolean shouldRestrictPlayerOptions(PlayerEntity player) {
        return player != null && !player.isSpectator() && !player.isCreative();
    }

}

// TODO: Add tasks
// TODO: Add death when off the train
// TODO: Nicer starting phase + UI
// TODO: Add letter item for detective and passenger
// TODO: System that remembers previous roles and allows cycling of roles
// TODO: Disable item drops
// TODO: Map reset system