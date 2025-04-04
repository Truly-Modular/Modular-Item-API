package smartin.miapi.fabric;

import dev.architectury.platform.Platform;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.loot.LootHelper;
import smartin.miapi.mixin.KeyMappingAccessor;
import smartin.miapi.mixin.OptionsAccessor;
import smartin.miapi.modules.properties.attributes.AttributeProperty;
import smartin.miapi.modules.properties.enchanment.AllowedEnchantments;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;

import static smartin.miapi.attributes.AttributeRegistry.SWIM_SPEED;

public class MiapiFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Miapi.init();
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            LootHelper.adjusted.forEach(tableBuilder::apply);
        });

        //DATA
        if (Environment.isClient()) {
            MiapiClientFabric.setupClient();
        }
        MiapiClient.KEY_BINDINGS.addCallback((key) -> {
            try {
                KeyBindingHelper.registerKeyBinding(key);
            } catch (RuntimeException e) {

            }
        });
        ReloadEvents.END.subscribe((isClient, registryAccess) -> {
            List<KeyMapping> mappings = new ArrayList<>(Arrays.stream(Minecraft.getInstance().options.keyMappings).toList());
            MiapiClient.KEY_BINDINGS.getFlatMap().forEach((id, key) -> {
                if (!mappings.contains(key)) {
                    mappings.add(key);
                }
                Set<String> categories = new HashSet<>(KeyMappingAccessor.getCATEGORIES());
                categories.add(key.getCategory());

                KeyMappingAccessor.setCATEGORIES(categories);
                Map<String, Integer> mapPrio = new HashMap<>(KeyMappingAccessor.getCATEGORY_SORT_ORDER());
                int max = Collections.max(mapPrio.values());
                if (!mapPrio.keySet().contains(key.getCategory())) {
                    mapPrio.put(key.getCategory(), max + 1);
                    KeyMappingAccessor.setCATEGORY_SORT_ORDER(mapPrio);
                }
            });
            ((OptionsAccessor) Minecraft.getInstance().options).setKeyMappings(mappings.toArray(new KeyMapping[0]));
        });

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
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableMiapiReloadListenerFixed());
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((minecraftServer, manager) -> {
            IdentifiableMiapiReloadListenerFixed.access = minecraftServer.reloadableRegistries().get();
            Miapi.registryAccess = minecraftServer.reloadableRegistries().get();
        });


        AttributeProperty.replaceMap.put("forge:generic.swim_speed", () -> SWIM_SPEED.value());

        loadCompat("zenith", smartin.miapi.fabric.compat.ZenithCompat::setup);
        loadCompat("treechop", smartin.miapi.fabric.compat.TrechopUtilFabric::loadTreechopCompat);
    }

    public static void loadCompat(String modId, Runnable onLoaded) {
        try {
            if (Platform.isModLoaded(modId)) {
                onLoaded.run();
            }
        } catch (RuntimeException e) {
            Miapi.LOGGER.error("could not setup compat for " + modId, e);
        }
    }
}