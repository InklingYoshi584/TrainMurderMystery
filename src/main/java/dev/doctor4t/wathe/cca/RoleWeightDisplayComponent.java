package dev.doctor4t.wathe.cca;

import dev.doctor4t.wathe.Wathe;
import dev.doctor4t.wathe.WatheConfig;
import dev.doctor4t.wathe.api.WatheRoles;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;

public class RoleWeightDisplayComponent implements ServerTickingComponent {
    public static final ComponentKey<RoleWeightDisplayComponent> KEY = ComponentRegistry.getOrCreate(
            Wathe.id("role_weight_display"), RoleWeightDisplayComponent.class
    );

    private final World world;
    private int tickCounter = 0;

    public RoleWeightDisplayComponent(World world) {
        this.world = world;
    }

    @Override
    public void serverTick() {
        // Check if the global toggle is enabled
        if (!WatheConfig.showRoleWeights) {
            return;
        }

        // Check if game is in pregame (INACTIVE status)
        GameWorldComponent gameComponent = GameWorldComponent.KEY.get(world);
        if (gameComponent.getGameStatus() != GameWorldComponent.GameStatus.INACTIVE) {
            return;
        }

        // Display role weights for all players every tick to prevent fading
        ScoreboardRoleSelectorComponent selector = ScoreboardRoleSelectorComponent.KEY.get(world.getScoreboard());
        
        // Cast to ServerWorld to get ServerPlayerEntity list
        if (world instanceof ServerWorld serverWorld) {
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                displayRoleWeightsForPlayer(player, selector, gameComponent);
            }
        }
    }

    private void displayRoleWeightsForPlayer(@NotNull ServerPlayerEntity player, @NotNull ScoreboardRoleSelectorComponent selector, @NotNull GameWorldComponent gameComponent) {
        // Get current role weights for the player
        int killerRounds = selector.killerRounds.getOrDefault(player.getUuid(), 0);
        int vigilanteRounds = selector.vigilanteRounds.getOrDefault(player.getUuid(), 0);
        
        // Calculate weights using the same formula as the actual game (linear decay)
        float killerWeight = 1.0f / (1.0f + killerRounds);
        float vigilanteWeight = 1.0f / (1.0f + vigilanteRounds);
        
        // Apply minimum weight (same as actual game)
        killerWeight = Math.max(killerWeight, 0.1f);
        vigilanteWeight = Math.max(vigilanteWeight, 0.1f);
        
        // Apply role repetition penalties if applicable
        // Use wasRoleLastRound method to check previous round roles
        if (gameComponent.wasRoleLastRound(player.getUuid(), WatheRoles.KILLER)) {
            killerWeight *= 0.7f; // 30% reduction for recent killers
        }
        if (gameComponent.wasRoleLastRound(player.getUuid(), WatheRoles.VIGILANTE)) {
            vigilanteWeight *= 0.5f; // 50% reduction for recent vigilantes
        }

        // Calculate percentages for display
        float totalWeight = killerWeight + vigilanteWeight + 1.0f; // Civilian weight is always 1.0
        float killerPercent = (killerWeight / totalWeight) * 100;
        float vigilantePercent = (vigilanteWeight / totalWeight) * 100;
        float civilianPercent = (1.0f / totalWeight) * 100;

        // Get role translations and remove exclamation marks if present
        String killerRoleName = removeExclamationMark(Text.translatable("announcement.role.killer").getString());
        String vigilanteRoleName = removeExclamationMark(Text.translatable("announcement.role.vigilante").getString());
        String civilianRoleName = removeExclamationMark(Text.translatable("announcement.role.civilian").getString());

        // Create action bar message with percentages
        MutableText actionBarText = Text.literal("")
                .append(Text.literal(killerRoleName + ": ").formatted(Formatting.RED))
                .append(Text.literal(String.format("%.1f%%", killerPercent)).formatted(Formatting.WHITE))
                .append(Text.literal(" | ").formatted(Formatting.GRAY))
                .append(Text.literal(vigilanteRoleName + ": ").formatted(Formatting.BLUE))
                .append(Text.literal(String.format("%.1f%%", vigilantePercent)).formatted(Formatting.WHITE))
                .append(Text.literal(" | ").formatted(Formatting.GRAY))
                .append(Text.literal(civilianRoleName + ": ").formatted(Formatting.GREEN))
                .append(Text.literal(String.format("%.1f%%", civilianPercent)).formatted(Formatting.WHITE));

        // Send to action bar
        player.sendMessage(actionBarText, true);
    }
    
    /**
     * Removes the exclamation mark from role names if present
     */
    private String removeExclamationMark(String roleName) {
        if (roleName.endsWith("!")) {
            return roleName.substring(0, roleName.length() - 1);
        }
        return roleName;
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        // No persistent data needed for this component
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        // No persistent data needed for this component
    }
}