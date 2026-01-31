package dev.doctor4t.wathe.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class CountBoardedPlayersCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("wathe:countBoardedPlayers")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> execute(context.getSource()))
        );
    }
    
    private static int execute(ServerCommandSource source) {
        // Get the ready area from the map variables component
        MapVariablesWorldComponent areas = MapVariablesWorldComponent.KEY.get(source.getWorld());
        
        // Count players who are in the ready area (considered "boarded")
        long boardedPlayerCount = source.getWorld().getPlayers().stream()
                .filter(player -> areas.getReadyArea().contains(player.getPos()))
                .count();
        
        // Return the count as an integer that can be used with execute store result
        return (int) boardedPlayerCount;
    }
}