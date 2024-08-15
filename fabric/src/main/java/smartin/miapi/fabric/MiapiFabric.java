package smartin.miapi.fabric;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import dev.architectury.platform.Platform;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.fabric.compat.ZenithCompat;
import smartin.miapi.modules.properties.AttributeProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static smartin.miapi.Miapi.MOD_ID;
import static smartin.miapi.attributes.AttributeRegistry.SWIM_SPEED;

public class MiapiFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Miapi.init();

        //DATA
        if (Environment.isClient()) {
            MiapiClientFabric.setupClient();
        }

        //ATTRIBUTE REPLACEMENT
        AttributeRegistry.ATTACK_RANGE = ReachEntityAttributes.ATTACK_RANGE;
        AttributeRegistry.REACH = ReachEntityAttributes.REACH;
        RegistryInventory.registerAtt("generic.swim_speed", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.swim_speed", 1.0, 0.0, 1024.0).setTracked(true),
                att -> SWIM_SPEED = att);

        if (Platform.isModLoaded("treechop")) {
            TrechopUtilFabric.loadTreechopCompat();
        }

        MiapiReloadListener listener = new MiapiReloadListener();
        RegistryInventory.modularItems.addCallback((modularItem) -> {
            ArmorRenderer.register(new ModularArmorRenderer(), modularItem);
        });

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(MOD_ID, "main_reload_listener");
            }

            public Collection<Identifier> getFabricDependencies() {
                return List.of(ResourceReloadListenerKeys.TAGS, ResourceReloadListenerKeys.RECIPES);
            }

            @Override
            public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
                return listener.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
            }
        });

        AttributeProperty.replaceMap.put("forge:generic.swim_speed", () -> SWIM_SPEED);
        AttributeProperty.replaceMap.put("miapi:generic.reach", () -> AttributeRegistry.REACH);
        AttributeProperty.replaceMap.put("miapi:generic.attack_range", () -> AttributeRegistry.ATTACK_RANGE);
        AttributeProperty.replaceMap.put("forge:block_reach", () -> AttributeRegistry.REACH);
        AttributeProperty.replaceMap.put("forge:entity_reach", () -> AttributeRegistry.ATTACK_RANGE);
        AttributeProperty.replaceMap.put("reach-entity-attributes:reach", () -> AttributeRegistry.REACH);
        AttributeProperty.replaceMap.put("reach-entity-attributes:attack_range", () -> AttributeRegistry.ATTACK_RANGE);

        if (Platform.isModLoaded("zenith")) {
            try {
                ZenithCompat.setup();
            } catch (RuntimeException surpressed) {
                Miapi.LOGGER.warn("couldnt load Zenith compat", surpressed);
            }
        }
    }
}