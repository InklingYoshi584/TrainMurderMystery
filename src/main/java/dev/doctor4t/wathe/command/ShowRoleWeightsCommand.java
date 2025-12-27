package dev.doctor4t.wathe.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.doctor4t.wathe.Wathe;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.ScoreboardRoleSelectorComponent;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class ShowRoleWeightsCommand {
    public static void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("wathe:showRoleWeights")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {
                            showRoleWeightsInActionBar(context.getSource());
                            return 1;
                        })
        );
    }

    private static void showRoleWeightsInActionBar(@NotNull ServerCommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendFeedback(() -> Text.literal("This command can only be used by players.").formatted(Formatting.RED), false);
            return;
        }

        ScoreboardRoleSelectorComponent selector = ScoreboardRoleSelectorComponent.KEY.get(source.getWorld().getScoreboard());
        GameWorldComponent gameComponent = GameWorldComponent.KEY.get(source.getWorld());

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
        if (gameComponent.wasRoleLastRound(player.getUuid(), WatheRoles.KILLER)) {
            killerWeight *= 0.7; // 30% reduction for recent killers
        }
        if (gameComponent.wasRoleLastRound(player.getUuid(), WatheRoles.VIGILANTE)) {
            vigilanteWeight *= 0.5; // 50% reduction for recent vigilantes
        }

        // Calculate percentages for display
        float totalWeight = killerWeight + vigilanteWeight + 1.0f; // Civilian weight is always 1.0
        float killerPercent = (killerWeight / totalWeight) * 100;
        float vigilantePercent = (vigilanteWeight / totalWeight) * 100;

        // Get role translations and remove exclamation marks if present
        String killerRoleName = removeExclamationMark(Text.translatable("announcement.role.killer").getString());
        String vigilanteRoleName = removeExclamationMark(Text.translatable("announcement.role.vigilante").getString());

        // Create action bar message with percentages
        MutableText actionBarText = Text.literal("")
                .append(Text.literal(killerRoleName + ": ").formatted(Formatting.RED))
                .append(Text.literal(String.format("%.1f%%", killerPercent)).formatted(Formatting.WHITE))
                .append(Text.literal(" | ").formatted(Formatting.GRAY))
                .append(Text.literal(vigilanteRoleName + ": ").formatted(Formatting.BLUE))
                .append(Text.literal(String.format("%.1f%%", vigilantePercent)).formatted(Formatting.WHITE));
        // Send to action bar
        player.sendMessage(actionBarText, true);
        
        // Also send feedback to chat
        source.sendFeedback(() -> Text.literal("Role weights displayed in your action bar.").formatted(Formatting.GRAY), false);
    }
    
    /**
     * Removes the exclamation mark from role names if present
     */
    private static String removeExclamationMark(String roleName) {
        return roleName.substring(0, roleName.length() - 1);
    }
}