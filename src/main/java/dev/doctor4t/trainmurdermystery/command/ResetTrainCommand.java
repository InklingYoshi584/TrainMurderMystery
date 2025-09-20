package dev.doctor4t.trainmurdermystery.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.doctor4t.trainmurdermystery.game.TMMGameLoop;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ResetTrainCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("tmm:forceReset")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {
                            TMMGameLoop.finalizeGame(context.getSource().getWorld());
                            return 1;
                        })
        );
    }
}
