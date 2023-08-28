package smartin.miapi.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.MinecraftClient;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.config.oro_config.screen.ConfigScreen;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> new ConfigScreen(MiapiConfig.getInstance()) {
        }.createScreen(MinecraftClient.getInstance().currentScreen);
    }
}
