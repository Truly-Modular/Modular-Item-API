package smartin.miapi.fabric;

import dev.architectury.platform.Platform;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.fabric.compat.ZenithCompat;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.attributes.AttributeProperty;
import smartin.miapi.modules.properties.enchanment.AllowedEnchantments;
import smartin.miapi.registries.RegistryInventory;

import static smartin.miapi.attributes.AttributeRegistry.SWIM_SPEED;

public class MiapiFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Miapi.init();

        //DATA
        if (Environment.isClient()) {
            MiapiClientFabric.setupClient();
        }
        EnchantmentEvents.ALLOW_ENCHANTING.register((enchantment, target, enchantingContext) -> {
            if (
                    ModularItem.isModularItem(target) &&
                    (AllowedEnchantments.isAllowed(target, enchantment, false))) {
                return TriState.TRUE;
            }
            return TriState.DEFAULT;
        });

        //ATTRIBUTE REPLACEMENT
        RegistryInventory.registerAtt("generic.swim_speed", true, () ->
                        new RangedAttribute("miapi.attribute.name.swim_speed", 1.0, 0.0, 1024.0).setSyncable(true),
                att -> SWIM_SPEED = att);

        if (Platform.isModLoaded("treechop")) {
            TrechopUtilFabric.loadTreechopCompat();
        }
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableMiapiReloadListener());

        AttributeProperty.replaceMap.put("forge:generic.swim_speed", () -> SWIM_SPEED.value());

        if (Platform.isModLoaded("zenith")) {
            try {
                ZenithCompat.setup();
            } catch (RuntimeException surpressed) {
                Miapi.LOGGER.warn("couldnt load Zenith compat", surpressed);
            }
        }
    }
}