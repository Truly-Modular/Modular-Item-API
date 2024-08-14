package smartin.miapi.fabric;

import dev.architectury.platform.Platform;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.fabric.compat.ZenithCompat;
import smartin.miapi.modules.properties.attributes.AttributeProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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
        RegistryInventory.registerAtt("generic.swim_speed", true, () ->
                        new RangedAttribute("miapi.attribute.name.swim_speed", 1.0, 0.0, 1024.0).setSyncable(true),
                att -> SWIM_SPEED = att);

        if (Platform.isModLoaded("treechop")) {
            TrechopUtilFabric.loadTreechopCompat();
        }

        MiapiReloadListener listener = new MiapiReloadListener();


        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return Miapi.id("main_reload_listener");
            }

            public Collection<ResourceLocation> getFabricDependencies() {
                return List.of(ResourceReloadListenerKeys.TAGS, ResourceReloadListenerKeys.RECIPES);
            }

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier synchronizer, ResourceManager manager, ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
                return listener.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
            }
        });

        AttributeProperty.replaceMap.put("forge:generic.swim_speed", () -> SWIM_SPEED.value());
        AttributeProperty.replaceMap.put("miapi:generic.reach", Attributes.BLOCK_INTERACTION_RANGE::value);
        AttributeProperty.replaceMap.put("miapi:generic.attack_range", Attributes.ENTITY_INTERACTION_RANGE::value);
        AttributeProperty.replaceMap.put("forge:block_reach", Attributes.BLOCK_INTERACTION_RANGE::value);
        AttributeProperty.replaceMap.put("forge:entity_reach", Attributes.ENTITY_INTERACTION_RANGE::value);
        AttributeProperty.replaceMap.put("reach-entity-attributes:reach", Attributes.BLOCK_INTERACTION_RANGE::value);
        AttributeProperty.replaceMap.put("reach-entity-attributes:attack_range", Attributes.ENTITY_INTERACTION_RANGE::value);

        if (Platform.isModLoaded("zenith")) {
            try {
                ZenithCompat.setup();
            } catch (RuntimeException surpressed) {
                Miapi.LOGGER.warn("couldnt load Zenith compat", surpressed);
            }
        }
    }
}