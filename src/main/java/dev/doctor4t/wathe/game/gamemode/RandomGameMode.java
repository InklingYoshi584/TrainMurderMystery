package dev.doctor4t.wathe.game.gamemode;

import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.TrainWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.UUID;

public class RandomGameMode extends GameMode {
    private GameMode selectedGameMode;
    
    public RandomGameMode(Identifier identifier) {
        super(identifier, 10, 2);
    }

    @Override
    public void initializeGame(ServerWorld serverWorld, GameWorldComponent gameWorldComponent, List<ServerPlayerEntity> players) {
        // Select game mode based on player count
        int playerCount = players.size();
        
        if (playerCount < 5) {
            // 2-4 players: Always Loose Ends
            selectedGameMode = WatheGameModes.LOOSE_ENDS;
        } else {
            // 5+ players: 80% Murder, 20% Loose Ends
            if (serverWorld.getRandom().nextFloat() < 0.8f) {
                selectedGameMode = WatheGameModes.MURDER;
            } else {
                selectedGameMode = WatheGameModes.LOOSE_ENDS;
            }
        }
        
        // Initialize the selected game mode
        selectedGameMode.initializeGame(serverWorld, gameWorldComponent, players);
    }

    @Override
    public void tickServerGameLoop(ServerWorld serverWorld, GameWorldComponent gameWorldComponent) {
        // Delegate to the selected game mode
        if (selectedGameMode != null) {
            selectedGameMode.tickServerGameLoop(serverWorld, gameWorldComponent);
        }
    }

    @Override
    public void finalizeGame(ServerWorld serverWorld, GameWorldComponent gameWorldComponent) {
        // Delegate to the selected game mode if it has custom finalization logic
        if (selectedGameMode != null) {
            selectedGameMode.finalizeGame(serverWorld, gameWorldComponent);
        }
    }

    @Override
    public void tickCommonGameLoop() {
        // Delegate to the selected game mode
        if (selectedGameMode != null) {
            selectedGameMode.tickCommonGameLoop();
        }
    }

    @Override
    public void tickClientGameLoop() {
        // Delegate to the selected game mode
        if (selectedGameMode != null) {
            selectedGameMode.tickClientGameLoop();
        }
    }
}