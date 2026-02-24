package dev.doctor4t.wathe.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.doctor4t.wathe.Wathe;
import dev.doctor4t.wathe.cca.TrainWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ResetTrainCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("wathe:resetTrain")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> execute(context.getSource()))
        );
    }

    private static int execute(ServerCommandSource source) {
        return Wathe.executeSupporterCommand(source,
                () -> {
                    TrainWorldComponent trainComponent = TrainWorldComponent.KEY.get(source.getWorld());
                    
                    boolean resetSuccess = GameFunctions.tryResetTrain(source.getWorld());
                    
                    // Send feedback to the command executor
                    source.sendFeedback(() -> Text.literal(resetSuccess ? "Train state has been reset to default values." : "Failed to reset train state.")
                            .formatted(resetSuccess ? Formatting.GREEN : Formatting.RED), false);
                }
        );
    }
}