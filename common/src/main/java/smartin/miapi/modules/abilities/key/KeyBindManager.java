package smartin.miapi.modules.abilities.key;

import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.registries.MiapiRegistry;

import java.util.HashMap;
import java.util.Map;

public final class KeyBindManager {
    public static final MiapiRegistry<MiapiBinding> REGISTRY =
            MiapiRegistry.getInstance(MiapiBinding.class);

    public static final ResourceLocation NONE =
            Miapi.id("none");

    public static void setup() {
        ReloadEvents.END.subscribe(
                (isClient, registryAccess, worker) -> {
                    if (isClient
                        && MiapiConfig.clientConfigObject != null) {
                        MiapiConfig.clientConfigObject.save();
                    }
                }
        );
    }

    public static void register(MiapiBinding binding) {
        if (!MiapiClient.KEY_BINDINGS.containsKey(binding.id)) {
            REGISTRY.register(binding.id, binding);
        }

        if (Platform.getEnv() == EnvType.CLIENT) {
            clientRegister(binding);
        }
    }

    public static void configLoad(
            Map<ResourceLocation, MiapiBinding> bindings
    ) {
        bindings.values().forEach(KeyBindManager::register);
    }

    @Environment(EnvType.CLIENT)
    public static void clientRegister(MiapiBinding binding) {
        if (binding.state.clientRegistered) {
            return;
        }
        binding.state.clientRegistered = true;
        if(!MiapiClient.KEY_BINDINGS.containsKey(binding.id)){
            MiapiClient.KEY_BINDINGS.register(binding.id, binding.asKeyMapping());
        }
        Map<ResourceLocation, MiapiBinding> bindingMap = new HashMap<>(MiapiConfig
                .getClientConfig()
                .other
                .bindings);
        if (bindingMap.containsKey(binding.id)) {
            return;
        }
        bindingMap.put(binding.id, binding);

        MiapiConfig
                .getClientConfig()
                .other
                .bindings = bindingMap;
    }
}