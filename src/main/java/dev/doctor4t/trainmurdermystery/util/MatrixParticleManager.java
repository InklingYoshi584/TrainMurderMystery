package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.client.TrainMurderMysteryClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public interface MatrixParticleManager {
    static Vec3d getMuzzlePosForPlayer(PlayerEntity playerEntity) {
        Vec3d pos = TrainMurderMysteryClient.particleMap.getOrDefault(playerEntity, null);
        TrainMurderMysteryClient.particleMap.remove(playerEntity);
        return pos;
    }

    static void setMuzzlePosForPlayer(PlayerEntity playerEntity, Vec3d vec3d) {
        TrainMurderMysteryClient.particleMap.put(playerEntity, vec3d);
    }
}
