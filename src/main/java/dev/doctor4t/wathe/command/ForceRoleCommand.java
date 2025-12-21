package dev.doctor4t.wathe.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.doctor4t.wathe.Wathe;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.ScoreboardRoleSelectorComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ForceRoleCommand {
    public static void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("wathe:forceRole").requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("killer").then(CommandManager.argument("players", EntityArgumentType.players())
                        .executes(context -> forceKiller(context.getSource(), EntityArgumentType.getPlayers(context, "players")))
                )).then(CommandManager.literal("vigilante").then(CommandManager.argument("players", EntityArgumentType.players())
                        .executes(context -> forceVigilante(context.getSource(), EntityArgumentType.getPlayers(context, "players")))
                ))
        );
    }

    private static int forceKiller(@NotNull ServerCommandSource source, @NotNull Collection<ServerPlayerEntity> players) {
        return Wathe.executeSupporterCommand(source,
                () -> {
                    ScoreboardRoleSelectorComponent component = ScoreboardRoleSelectorComponent.KEY.get(source.getServer().getScoreboard());
                    component.forcedKillers.clear();
                    for (ServerPlayerEntity player : players) component.forcedKillers.add(player.getUuid());
                    
                    // Auto-start a new game to apply forced roles immediately
                    if (GameWorldComponent.KEY.get(source.getWorld()).isRunning()) {
                        GameFunctions.finalizeGame(source.getWorld());
                    }
                    GameFunctions.initializeGame(source.getWorld());
                    
                    source.sendFeedback(() -> Text.literal("Forced " + players.size() + " players as killers and started new game").formatted(), false);
                }
        );
    }

    private static int forceVigilante(@NotNull ServerCommandSource source, @NotNull Collection<ServerPlayerEntity> players) {
        return Wathe.executeSupporterCommand(source,
                () -> {
                    ScoreboardRoleSelectorComponent component = ScoreboardRoleSelectorComponent.KEY.get(source.getServer().getScoreboard());
                    component.forcedVigilantes.clear();
                    for (ServerPlayerEntity player : players) component.forcedVigilantes.add(player.getUuid());
                    
                    // Auto-start a new game to apply forced roles immediately
                    if (GameWorldComponent.KEY.get(source.getWorld()).isRunning()) {
                        GameFunctions.finalizeGame(source.getWorld());
                    }
                    GameFunctions.initializeGame(source.getWorld());
                    
                    source.sendFeedback(() -> Text.literal("Forced " + players.size() + " players as vigilantes and started new game").formatted(), false);
                }
        );
    }
}