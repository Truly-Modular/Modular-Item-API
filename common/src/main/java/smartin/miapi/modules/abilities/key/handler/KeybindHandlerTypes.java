package smartin.miapi.modules.abilities.key.handler;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class KeybindHandlerTypes {
    public static final Map<ResourceLocation, KeybindHandlerType<?>> REGISTRY =
            new HashMap<>();

    public static <T extends KeybindHandler> KeybindHandlerType<T> register(
            KeybindHandlerType<T> type
    ) {
        REGISTRY.put(type.id(), type);
        return type;
    }
}