package smartin.miapi.modules.abilities.key;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.modules.abilities.key.handler.BindingState;
import smartin.miapi.modules.abilities.key.handler.KeybindHandler;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;

import java.util.Collection;

public class ClientKeybinding {
    public static void clientTick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;
        if (!MiapiConfig.getServerConfig().other.developmentMode) {
            return;
        }
        ItemAbilityManager.clientKeyBindID.remove(player);
        Collection<MiapiBinding> bindings =
                KeyBindManager.REGISTRY.getFlatMap().values();


        for (MiapiBinding binding : bindings) {
            BindingState state = binding.state;
            KeyMapping key = binding.asKeyMapping();

            boolean down = key.isDown();
            boolean pressed = down && !state.lastPressed;
            boolean released = !down && state.lastPressed;

            state.pressed = down;
            state.lastPressed = down;

            if (down) {
                state.holdTicks++;
            } else {
                state.holdTicks = 0;
            }
            if (pressed) {
                state.lastPressTime = System.currentTimeMillis();
            }
            if (released) {
                state.lastReleaseTime = System.currentTimeMillis();
            }
            KeybindHandler handler = binding.handler;
            if (pressed) {
                handler.onPress(client, player, binding);
            }
            if (released) {
                handler.onRelease(client, player, binding);
            }
            if (down) {
                handler.whileHeld(client, player, binding);
            }
        }
    }
}
