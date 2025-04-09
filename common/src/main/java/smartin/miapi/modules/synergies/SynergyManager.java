package smartin.miapi.modules.synergies;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.PropertyHolder;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.conditions.ModuleCondition;
import smartin.miapi.modules.properties.TagProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SynergyManager {
    public static final Map<ResourceLocation, MapCodec<? extends Synergy>> SYNERGY_TYPE_REGISTRY = new ConcurrentHashMap<>();
    protected static final Map<ResourceLocation, List<Synergy>> moduleSynergies = new ConcurrentHashMap<>();
    protected static final Map<ResourceLocation, List<Synergy>> materialSynergies = new ConcurrentHashMap<>();
    protected static final Map<String, List<Synergy>> tagSynergies = new ConcurrentHashMap<>();
    public static final Codec<Synergy> SYNERGY_CODEC = Miapi.ID_CODEC.dispatch(Synergy::getType, SynergyManager::getType);

    public static void setup() {
        SYNERGY_TYPE_REGISTRY.put(Miapi.id("module"), ModuleSynergy.CODEC);
        SYNERGY_TYPE_REGISTRY.put(Miapi.id("material"), MaterialSynergy.CODEC);
        SYNERGY_TYPE_REGISTRY.put(Miapi.id("tag"), TagSynergy.CODEC);
        PropertyResolver.register("synergies", (moduleInstance, oldMap) -> {
            if (moduleInstance != null) {
                var synergies = moduleSynergies.get(moduleInstance.module.id());
                if (synergies != null) {
                    for (Synergy synergy : synergies) {
                        oldMap = synergy.apply(moduleInstance, oldMap);
                    }
                }
                for (String tag : TagProperty.getTags(moduleInstance)) {
                    var tagSynergy = tagSynergies.get(tag);
                    if (tagSynergy != null) {
                        for (Synergy synergy : tagSynergy) {
                            oldMap = synergy.apply(moduleInstance, oldMap);
                        }
                    }
                }
                Material material = MaterialProperty.getMaterial(moduleInstance);
                if (material != null) {
                    var materialSynergy = materialSynergies.get(material.getID());
                    if (materialSynergy != null) {
                        for (Synergy synergy : materialSynergy) {
                            oldMap = synergy.apply(moduleInstance, oldMap);
                        }
                    }
                }
            }
            return oldMap;
        });

        ReloadEvents.END.subscribe((isClient, registryAccess) -> {
            int totalSynergies = moduleSynergies.values().stream()
                                         .mapToInt(List::size)
                                         .sum() + materialSynergies.values().stream()
                                         .mapToInt(List::size)
                                         .sum() + tagSynergies.values().stream()
                                         .mapToInt(List::size)
                                         .sum();
            Miapi.LOGGER.info("Loaded " + totalSynergies + " Synergies");
        });
    }

    public static MapCodec<? extends Synergy> getType(ResourceLocation id) {
        var codec = SYNERGY_TYPE_REGISTRY.get(id);
        if (codec == null) {
            try {
                throw new DecoderException("Synergy Type " + id + " does not exist!");
            } catch (DecoderException e) {
                throw new RuntimeException(e);
            }
        }
        return codec;
    }

    public static void clear() {
        moduleSynergies.clear();
        materialSynergies.clear();
        tagSynergies.clear();
    }

    public abstract static class Synergy {

        protected ModuleCondition condition;
        protected PropertyHolder holder;
        protected ResourceLocation id;

        protected abstract ResourceLocation getType();

        public abstract void register();

        public Map<ModuleProperty<?>, Object> apply(ModuleInstance moduleInstance, Map<ModuleProperty<?>, Object> properties) {
            if (condition.isAllowed(ConditionManager.moduleContext(moduleInstance, properties))) {
                return holder.applyHolder(properties, Optional.of(Component.translatable("miapi.property.source.synergy")));
            }
            return properties;
        }
    }

}
