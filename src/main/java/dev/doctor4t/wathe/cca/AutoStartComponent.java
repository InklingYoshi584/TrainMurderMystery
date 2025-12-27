package dev.doctor4t.wathe.cca;

import dev.doctor4t.wathe.Wathe;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.api.WatheMapEffects;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

public class AutoStartComponent implements AutoSyncedComponent, CommonTickingComponent {
    public static final ComponentKey<AutoStartComponent> KEY = ComponentRegistry.getOrCreate(Wathe.id("autostart"), AutoStartComponent.class);
    public final World world;
    public int startTime;
    public int time;

    public AutoStartComponent(World world) {
        this.world = world;
    }

    public void sync() {
        KEY.sync(this.world);
    }

    public void reset() {
        this.setTime(this.startTime);
    }

    @Override
    public void tick() {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(this.world);
        if (gameWorldComponent.isRunning()) return;

        if (this.startTime <= 0 && this.time <= 0) return;

        GameMode storedGameMode = gameWorldComponent.getGameMode();
        int playerCount = GameFunctions.getReadyPlayerCount(world);
        
        // Calculate the actual game mode if original mode was random or stored is random
        GameMode actualGameMode = storedGameMode;
        if (gameWorldComponent.isOriginalModeRandom() || actualGameMode == WatheGameModes.RANDOM) {
            // Calculate actual game mode based on player count and random chance
            if (playerCount < 5 || world.getRandom().nextFloat() < 0.2f) {
                // 2-4 players: Always Loose Ends, or 20% chance for 5+ players
                actualGameMode = WatheGameModes.LOOSE_ENDS;
            } else {
                // 80% chance for 5+ players: Murder
                actualGameMode = WatheGameModes.MURDER;
            }
        }
        
        // Check if we have enough players for the actual game mode
        if (playerCount >= actualGameMode.minPlayerCount) {
            if (this.time-- <= 0 && this.world instanceof ServerWorld serverWorld) {
                if (gameWorldComponent.getGameStatus() == GameWorldComponent.GameStatus.INACTIVE) {
                    // Save the original random mode flag
                    boolean wasOriginalModeRandom = gameWorldComponent.isOriginalModeRandom();
                    // Start the game with the actual game mode
                    GameFunctions.startGame(serverWorld, actualGameMode, gameWorldComponent.getMapEffect(), GameConstants.getInTicks(actualGameMode.defaultStartTime, 0));
                    // Restore the original random mode flag
                    if (wasOriginalModeRandom) {
                        GameWorldComponent.KEY.get(serverWorld).setOriginalModeRandom(true);
                    }
                    return;
                }
            }

            if (this.getTime() % 20 == 0) {
                this.sync();
            }
        } else {
            if (this.world.getTime() % 20 == 0) {
                this.setTime(this.startTime);
            }
        }
    }

    public boolean isAutoStartActive() {
        return startTime > 0;
    }

    public boolean hasTime() {
        return this.time > 0;
    }

    public int getTime() {
        return this.time;
    }

    public void addTime(int time) {
        this.setTime(this.time + time);
    }

    public void setStartTime(int time) {
        this.startTime = time;
    }

    public void setTime(int time) {
        this.time = time;
        this.sync();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("startTime", this.startTime);
        tag.putInt("time", this.time);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.startTime = tag.getInt("startTime");
        this.time = tag.getInt("time");
    }
}