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

@Environment(EnvType.CLIENT)
public class MiapiClient{

    public static void setupClient(){
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new CustomModelRegistry());
    }
}
