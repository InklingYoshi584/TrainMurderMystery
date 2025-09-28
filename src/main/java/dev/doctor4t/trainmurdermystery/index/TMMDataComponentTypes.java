package dev.doctor4t.trainmurdermystery.index;

import com.mojang.serialization.Codec;
import dev.doctor4t.trainmurdermystery.TMM;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public interface TMMDataComponentTypes {
    ComponentType<Boolean> POISONED = register("poisoned", booleanBuilder -> booleanBuilder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL));
    ComponentType<String> POISONER = register("poisoner", stringBuilder -> stringBuilder.codec(Codec.STRING).packetCodec(PacketCodecs.STRING));

    private static <T> ComponentType<T> register(String name, @NotNull UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, TMM.id(name), builderOperator.apply(ComponentType.builder()).build());
    }
}
