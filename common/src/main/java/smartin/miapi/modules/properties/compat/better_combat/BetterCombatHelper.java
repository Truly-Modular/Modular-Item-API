package smartin.miapi.modules.properties.compat.better_combat;


import dev.architectury.platform.Platform;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.render.ServerReplaceProperty;

import static smartin.miapi.registries.RegistryInventory.moduleProperties;
import static smartin.miapi.registries.RegistryInventory.registerMiapi;

public class BetterCombatHelper {
    public static ResourceLocation KEY = Miapi.id("better_combat_config");
    public static void setup() {
        if(Platform.isModLoaded("bettercombat")){
            registerMiapi(moduleProperties, KEY, new BetterCombatProperty());
        }else{
            registerMiapi(moduleProperties, KEY, new ServerReplaceProperty());
        }
    }
}