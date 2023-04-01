package smartin.miapi.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import smartin.miapi.Miapi;
import smartin.miapi.client.ClientInit;

@Environment(EnvType.CLIENT)
public class MiapiClient implements ClientModInitializer {

    public static final KeyBinding OPEN_GUI_KEY_BINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.example.open_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.example"
    ));

    @Override
    public void onInitializeClient() {
        //Networking.setImplementation(new NetworkingImplFabric());
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new CustomModelRegistry());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_GUI_KEY_BINDING.wasPressed()) {
                Miapi.server.getPlayerManager().getPlayerList().forEach(serverPlayer->{
                    serverPlayer.openHandledScreen(ClientInit.test());
                });
            }
        });
    }

    public static void setupClient(){
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new CustomModelRegistry());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_GUI_KEY_BINDING.wasPressed()) {
            }
        });
    }
}
