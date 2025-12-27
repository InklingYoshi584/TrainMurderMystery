package dev.doctor4t.wathe.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.doctor4t.wathe.Wathe;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.MapEffect;
import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.api.WatheMapEffects;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import dev.doctor4t.wathe.command.argument.GameModeArgumentType;
import dev.doctor4t.wathe.command.argument.MapEffectArgumentType;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class StartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("wathe:start")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("gameMode", GameModeArgumentType.gameMode())
                                .then(CommandManager.argument("mapEffect", MapEffectArgumentType.mapEffect())
                                        .then(CommandManager.argument("startTimeInMinutes", IntegerArgumentType.integer(1))
                                                .executes(context -> execute(context.getSource(), GameModeArgumentType.getGameModeArgument(context, "gameMode"), MapEffectArgumentType.getMapEffectArgument(context, "mapEffect"), IntegerArgumentType.getInteger(context, "startTimeInMinutes")))
                                        )
                                        .executes(context -> execute(
                                                        context.getSource(),
                                                        GameModeArgumentType.getGameModeArgument(context, "gameMode"),
                                                        MapEffectArgumentType.getMapEffectArgument(context, "mapEffect"),
                                                        -1
                                                )
                                        )
                                )
                        )
        );
    }

    private static int execute(ServerCommandSource source, GameMode gameMode, MapEffect mapEffect, int minutes) {
        if (GameWorldComponent.KEY.get(source.getWorld()).isRunning()) {
            source.sendError(Text.translatable("game.start_error.game_running"));
            return -1;
        }
        
        // Check if original mode was random
        boolean isOriginalRandom = gameMode == WatheGameModes.RANDOM;
        
        // Calculate actual game mode if random is selected
        GameMode actualGameMode = gameMode;
        if (isOriginalRandom) {
            MapVariablesWorldComponent areas = MapVariablesWorldComponent.KEY.get(source.getWorld());
            int playerCount = Math.toIntExact(source.getWorld().getPlayers().stream().filter(serverPlayerEntity -> (areas.getReadyArea().contains(serverPlayerEntity.getPos()))).count());
            
            // Select actual game mode based on player count
            if (playerCount < 5 || source.getWorld().getRandom().nextFloat() < 0.2f) {
                // 2-4 players: Always Loose Ends, or 20% chance for 5+ players
                actualGameMode = WatheGameModes.LOOSE_ENDS;
            } else {
                // 80% chance for 5+ players: Murder
                actualGameMode = WatheGameModes.MURDER;
            }
        }
        
        // Create final copies for lambda expression
        final GameMode finalGameMode = actualGameMode;
        final MapEffect finalMapEffect = mapEffect;
        final int finalMinutes = minutes;
        final boolean finalIsOriginalRandom = isOriginalRandom;
        
        // Function to start the game with preserved original random mode flag
        Runnable startGameTask = () -> {
            ServerWorld world = source.getWorld();
            GameFunctions.startGame(world, finalGameMode, finalMapEffect, GameConstants.getInTicks(finalMinutes >= 0 ? finalMinutes : finalGameMode.defaultStartTime, 0));
            // Set the original mode random flag if needed
            if (finalIsOriginalRandom) {
                GameWorldComponent.KEY.get(world).setOriginalModeRandom(true);
            }
        };
        
        if (finalGameMode == WatheGameModes.LOOSE_ENDS || finalGameMode == WatheGameModes.DISCOVERY || finalMapEffect == WatheMapEffects.HARPY_EXPRESS_SUNDOWN || finalMapEffect == WatheMapEffects.HARPY_EXPRESS_DAY) {
            return Wathe.executeSupporterCommand(source, startGameTask);
        } else  {
            startGameTask.run();
            return 1;
        }
    }
}
