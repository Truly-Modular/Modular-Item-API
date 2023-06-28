package smartin.miapi.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.common.ForgeMod;
import smartin.miapi.Miapi;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.properties.AttributeProperty;

@Mod(Miapi.MOD_ID)
public class TrulyModularForge {
    public TrulyModularForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(Miapi.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Miapi.init();

        //ATTRIBUTE REPLACEMENT
        AttributeProperty.replaceMap.put("miapi:generic.reach", ForgeMod.BLOCK_REACH);
        AttributeProperty.replaceMap.put("miapi:generic.attack_range", ForgeMod.ENTITY_REACH);
    }
}