package smartin.miapi.modules.abilities.key.handler;

import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.modules.abilities.key.MiapiBinding;

public interface KeybindHandler {
    Codec<KeybindHandler> HANDLER_CODEC =
            ResourceLocation.CODEC.dispatch(
                    handler -> handler.type().id(),
                    id -> {
                        KeybindHandlerType<?> type =
                                KeybindHandlerTypes.REGISTRY.get(id);
                        if (type == null) {
                            throw new IllegalStateException(
                                    "Unknown handler type: " + id
                            );
                        }
                        return type.codec();
                    }
            );

    default void tick(Minecraft minecraft, LocalPlayer player, MiapiBinding binding)
    {}

    default void onPress(
            Minecraft minecraft,
            LocalPlayer player,
            MiapiBinding binding
    ) {
    }

    default void onRelease(
            Minecraft minecraft,
            LocalPlayer player,
            MiapiBinding binding
    ) {
    }

    KeybindHandlerType<?> type();

    default void whileHeld(
            Minecraft minecraft,
            LocalPlayer player,
            MiapiBinding binding
    ) {
    }
}