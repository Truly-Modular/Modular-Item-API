package smartin.miapi.forge;

import dev.architectury.platform.forge.EventBuses;
import smartin.miapi.Miapi;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Miapi.MOD_ID)
public class TrulyModularForge {
    public TrulyModularForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(Miapi.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Miapi.init();
    }
}