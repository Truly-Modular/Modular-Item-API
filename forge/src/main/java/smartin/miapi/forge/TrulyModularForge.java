package smartin.miapi.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.properties.AttributeProperty;

import static smartin.miapi.Miapi.MOD_ID;

@Mod(MOD_ID)
public class TrulyModularForge {
    public TrulyModularForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Miapi.init();

        //ATTRIBUTE REPLACEMENT
        //AttributeRegistry.REACH = ForgeMod.BLOCK_REACH.getRaw();
        //AttributeRegistry.ATTACK_RANGE = ForgeMod.ENTITY_REACH.getRaw();
        AttributeProperty.replaceMap.put("miapi:generic.reach", ForgeMod.BLOCK_REACH);
        AttributeProperty.replaceMap.put("miapi:generic.attack_range", ForgeMod.ENTITY_REACH);
        AttributeProperty.replaceMap.put("forge:block_reach", ForgeMod.BLOCK_REACH);
        AttributeProperty.replaceMap.put("forge:entity_reach", ForgeMod.ENTITY_REACH);
        AttributeProperty.replaceMap.put("reach-entity-attributes:reach", ForgeMod.BLOCK_REACH);
        AttributeProperty.replaceMap.put("reach-entity-attributes:attack_range", ForgeMod.ENTITY_REACH);
        //AttributeRegistry.ATTACK_RANGE = ForgeMod.ENTITY_REACH.getRaw();
        //AttributeProperty.priorityMap.put(ForgeMod.BLOCK_REACH, -7.0f);
        //AttributeProperty.priorityMap.put(ForgeMod.ENTITY_REACH, -7.0f);
    }


    public static class ClientEvents {
        @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
        public static class ModBus {
            @SubscribeEvent
            public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
                AttributeRegistry.REACH = ForgeMod.BLOCK_REACH.get();
                AttributeRegistry.ATTACK_RANGE = ForgeMod.ENTITY_REACH.get();
            }
        }
    }
}