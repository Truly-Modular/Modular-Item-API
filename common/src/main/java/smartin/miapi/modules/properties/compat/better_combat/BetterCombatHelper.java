package smartin.miapi.modules.properties.compat.better_combat;


import dev.architectury.platform.Platform;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.render.ServerReplaceProperty;

import static smartin.miapi.registries.RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY;
import static smartin.miapi.registries.RegistryInventory.registerMiapi;

public class BetterCombatHelper {
    public static ResourceLocation KEY = Miapi.id("better_combat_config");
    public static void setup() {
        if(Platform.isModLoaded("bettercombat")){
            registerMiapi(MODULE_PROPERTY_MIAPI_REGISTRY, KEY, new BetterCombatProperty());
        }else{
            registerMiapi(MODULE_PROPERTY_MIAPI_REGISTRY, KEY, new ServerReplaceProperty());
        }
    }
}