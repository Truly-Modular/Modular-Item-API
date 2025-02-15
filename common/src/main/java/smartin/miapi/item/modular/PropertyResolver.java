package smartin.miapi.item.modular;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages Properties for Items
 */
public class PropertyResolver {
    public static List<Tuple<ResourceLocation, PropertyProvider>> registry = Collections.synchronizedList(new ArrayList<>());

    /**
     * Resolves {@link ModuleProperty} maps for an {@link ModuleInstance}
     *
     * @param moduleInstance the {@link ModuleInstance} to resolve for
     * @return a map of {@link ModuleProperty} and their related Data
     */
    public static void resolve(ModuleInstance moduleInstance) {
        if (moduleInstance.properties == null) {
            moduleInstance.properties = new ConcurrentHashMap<>();
        }
        synchronized (moduleInstance.properties) {
            registry.forEach((pair) -> {
                PropertyProvider propertyProvider = pair.getB();
                moduleInstance.allSubModules().forEach(instance -> {
                    if (instance.properties == null) {
                        instance.properties = new ConcurrentHashMap<>();
                    }
                    instance.properties.putAll(propertyProvider.resolve(instance, instance.properties));
                });
            });
        }
    }

    /**
     * This method can be used to register new {@link PropertyProvider} at any position in the resolving chain
     *
     * @param identifier       the ID of the new {@link PropertyProvider}
     * @param propertyProvider the {@link PropertyProvider} to register
     * @param before           all the entries that should resolve after this entry, it will be registered as late as possible
     * @return the now registered {@link PropertyProvider}
     */
    public static PropertyProvider register(ResourceLocation identifier, PropertyProvider propertyProvider, Collection<ResourceLocation> before) {
        Tuple<ResourceLocation, PropertyProvider> entry = new Tuple<>(identifier, propertyProvider);

        // Remove existing entries with the same identifier
        registry.removeIf(pair -> pair.getA().equals(identifier));

        // Find position to insert after identifiers
        int index = 0;
        for (Tuple<ResourceLocation, PropertyProvider> pair : registry) {
            if (before.contains(pair.getA())) {
                registry.add(index, entry);
                return propertyProvider;
            }
            index++;
        }

        // If after identifiers are not found, append at the end
        registry.add(entry);
        return propertyProvider;
    }

    public static PropertyProvider register(ResourceLocation identifier, PropertyProvider propertyProvider) {
        registry.add(new Tuple<>(identifier, propertyProvider));
        return propertyProvider;
    }

    public static PropertyProvider register(String identifier, PropertyProvider propertyProvider) {
        return register(Miapi.id(identifier), propertyProvider);
    }

    public static Map<ModuleProperty<?>, Object> merge(Map<ModuleProperty<?>, Object> old, Map<ModuleProperty<?>, Object> toMerge, MergeType mergeType) {
        Map<ModuleProperty<?>, Object> merged = new LinkedHashMap<>(old);
        toMerge.forEach(((property, element) -> {
            if (merged.containsKey(property)) {
                merged.put(property, ItemModule.merge(property, merged.get(property), element, mergeType));
            } else {
                merged.put(property, element);
            }
        }));
        return merged;
    }

    /**
     * This interface allows other classes to add Properties to items
     */
    public interface PropertyProvider {
        Map<ModuleProperty<?>, Object> resolve(ModuleInstance moduleInstance, Map<ModuleProperty<?>, Object> properties);
    }
}
