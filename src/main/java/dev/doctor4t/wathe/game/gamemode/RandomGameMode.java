package dev.doctor4t.wathe.game.gamemode;

import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.*;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class RandomGameMode extends GameMode {
    private boolean isMurderMode;
    
    public RandomGameMode(Identifier identifier) {
        super(identifier, 10, 2);
    }

    private int assignRolesAndGetKillerCount(@NotNull ServerWorld world, @NotNull List<ServerPlayerEntity> players, GameWorldComponent gameComponent) {
        // civilian base role, replaced for selected killers and vigilantes
        for (ServerPlayerEntity player : players) {
            gameComponent.addRole(player, WatheRoles.CIVILIAN);
        }

        // select roles
        ScoreboardRoleSelectorComponent roleSelector = ScoreboardRoleSelectorComponent.KEY.get(world.getScoreboard());
        
        // Calculate killer count based on configuration
        int killerCount;
        int configuredKillerCount = gameComponent.getNextRoundKillerCount();
        
        if (configuredKillerCount > 0) {
            // Use exact configured count for next round
            killerCount = configuredKillerCount;
            // Reset the configured count for future rounds
            gameComponent.setNextRoundKillerCount(0);
        } else {
            // Use ratio-based calculation
            int ratio = gameComponent.getKillerPlayerRatio();
            killerCount = (int) Math.floor(players.size() / (float) ratio);
        }
        
        // Ensure at least 1 killer if there are enough players
        killerCount = Math.max(1, killerCount);
        
        int total = roleSelector.assignKillers(world, gameComponent, players, killerCount);
        roleSelector.assignVigilantes(world, gameComponent, players, killerCount);
        return total;
    }

    @Override
    public void initializeGame(ServerWorld serverWorld, GameWorldComponent gameWorldComponent, List<ServerPlayerEntity> players) {
        // Select game mode based on player count
        int playerCount = players.size();
        
        if (playerCount < 5) {
            // 2-4 players: Always Loose Ends
            isMurderMode = false;
            
            // Loose Ends initialization
            for (ServerPlayerEntity player : players) {
                player.getInventory().clear();

                ItemStack derringer = new ItemStack(WatheItems.DERRINGER);
                ItemStack knife = new ItemStack(WatheItems.KNIFE);

                int cooldown = GameConstants.getInTicks(1, 0);
                ItemCooldownManager itemCooldownManager = player.getItemCooldownManager();
                itemCooldownManager.set(WatheItems.DERRINGER, cooldown);
                itemCooldownManager.set(WatheItems.KNIFE, cooldown);

                player.giveItemStack(new ItemStack(WatheItems.CROWBAR));
                player.giveItemStack(derringer);
                player.giveItemStack(knife);

                gameWorldComponent.addRole(player, WatheRoles.LOOSE_END);

                ServerPlayNetworking.send(player, new AnnounceWelcomePayload(RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(RoleAnnouncementTexts.LOOSE_END), -1, -1));
            }
        } else {
            // 5+ players: 80% Murder, 20% Loose Ends
            if (serverWorld.getRandom().nextFloat() < 0.8f) {
                isMurderMode = true;
                
                // Murder initialization
                int killerCount = assignRolesAndGetKillerCount(serverWorld, players, gameWorldComponent);

                for (ServerPlayerEntity player : players) {
                    ServerPlayNetworking.send(player, new AnnounceWelcomePayload(RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(gameWorldComponent.isRole(player, WatheRoles.KILLER) ? RoleAnnouncementTexts.KILLER : gameWorldComponent.isRole(player, WatheRoles.VIGILANTE) ? RoleAnnouncementTexts.VIGILANTE : RoleAnnouncementTexts.CIVILIAN), killerCount, players.size() - killerCount));
                }
            } else {
                isMurderMode = false;
                
                // Loose Ends initialization
                for (ServerPlayerEntity player : players) {
                    player.getInventory().clear();

                    ItemStack derringer = new ItemStack(WatheItems.DERRINGER);
                    ItemStack knife = new ItemStack(WatheItems.KNIFE);

                    int cooldown = GameConstants.getInTicks(1, 0);
                    ItemCooldownManager itemCooldownManager = player.getItemCooldownManager();
                    itemCooldownManager.set(WatheItems.DERRINGER, cooldown);
                    itemCooldownManager.set(WatheItems.KNIFE, cooldown);

                    player.giveItemStack(new ItemStack(WatheItems.CROWBAR));
                    player.giveItemStack(derringer);
                    player.giveItemStack(knife);

                    gameWorldComponent.addRole(player, WatheRoles.LOOSE_END);

                    ServerPlayNetworking.send(player, new AnnounceWelcomePayload(RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(RoleAnnouncementTexts.LOOSE_END), -1, -1));
                }
            }
        }
    }

    @Override
    public void tickServerGameLoop(ServerWorld serverWorld, GameWorldComponent gameWorldComponent) {
        if (isMurderMode) {
            // Murder game loop
            GameFunctions.WinStatus winStatus = GameFunctions.WinStatus.NONE;

            // check if out of time
            if (!GameTimeComponent.KEY.get(serverWorld).hasTime())
                winStatus = GameFunctions.WinStatus.TIME;

            boolean civilianAlive = false;
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                // passive money
                if (gameWorldComponent.canUseKillerFeatures(player)) {
                    Integer balanceToAdd = GameConstants.PASSIVE_MONEY_TICKER.apply(serverWorld.getTime());
                    if (balanceToAdd > 0) PlayerShopComponent.KEY.get(player).addToBalance(balanceToAdd);
                }

                // check if some civilians are still alive
                if (gameWorldComponent.isInnocent(player) && !GameFunctions.isPlayerEliminated(player)) {
                    civilianAlive = true;
                }
            }

            // check killer win condition (killed all civilians)
            if (!civilianAlive) {
                winStatus = GameFunctions.WinStatus.KILLERS;
            }

            // check passenger win condition (all killers are dead)
            if (winStatus == GameFunctions.WinStatus.NONE) {
                winStatus = GameFunctions.WinStatus.PASSENGERS;
                for (UUID player : gameWorldComponent.getAllKillerTeamPlayers()) {
                    if (!GameFunctions.isPlayerEliminated(serverWorld.getPlayerByUuid(player))) {
                        winStatus = GameFunctions.WinStatus.NONE;
                    }
                }
            }

            // game end on win and display
            if (winStatus != GameFunctions.WinStatus.NONE && gameWorldComponent.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE) {
                GameRoundEndComponent.KEY.get(serverWorld).setRoundEndData(serverWorld.getPlayers(), winStatus);

                GameFunctions.stopGame(serverWorld);
            }
        } else {
            // Loose Ends game loop
            GameFunctions.WinStatus winStatus = GameFunctions.WinStatus.NONE;

            // check if out of time
            if (!GameTimeComponent.KEY.get(serverWorld).hasTime())
                winStatus = GameFunctions.WinStatus.TIME;

            // check if last person standing in loose end
            int playersLeft = 0;
            PlayerEntity lastPlayer = null;
            for (PlayerEntity player : serverWorld.getPlayers()) {
                if (GameFunctions.isPlayerAliveAndSurvival(player)) {
                    playersLeft++;
                    lastPlayer = player;
                }
            }

            if (playersLeft <= 0) {
                GameFunctions.stopGame(serverWorld);
            }

            if (playersLeft == 1) {
                gameWorldComponent.setLooseEndWinner(lastPlayer.getUuid());
                winStatus = GameFunctions.WinStatus.LOOSE_END;
            }

            // game end on win and display
            if (winStatus != GameFunctions.WinStatus.NONE && gameWorldComponent.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE) {
                GameRoundEndComponent.KEY.get(serverWorld).setRoundEndData(serverWorld.getPlayers(), winStatus);

                GameFunctions.stopGame(serverWorld);
            }
        }
    }

    @Override
    public void tickCommonGameLoop() {
        // Common game loop logic if needed
    }

    @Override
    public void tickClientGameLoop() {
        // Client game loop logic if needed
    }

    @Override
    public void finalizeGame(ServerWorld serverWorld, GameWorldComponent gameWorldComponent) {
        // Finalization logic if needed
    }
}