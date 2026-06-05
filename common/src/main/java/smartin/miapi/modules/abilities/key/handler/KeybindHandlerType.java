package smartin.miapi.modules.abilities.key.handler;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

public interface KeybindHandlerType<T extends KeybindHandler> {
    ResourceLocation id();

    MapCodec<T> codec();

    record SimpleType<T extends KeybindHandler>(ResourceLocation id, MapCodec<T> codec) implements KeybindHandlerType<T>{

    }
}