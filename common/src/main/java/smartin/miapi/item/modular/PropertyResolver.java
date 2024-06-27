package smartin.miapi.item.modular;

import com.google.gson.JsonElement;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;

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
        moduleInstance.allSubModules().forEach(instance -> {
            if (instance.rawProperties == null) {
                instance.rawProperties = new ConcurrentHashMap<>();
            }
        });
        registry.forEach((pair) -> {
            PropertyProvider propertyProvider = pair.getB();
            moduleInstance.allSubModules().forEach(instance -> {
                if (instance.rawProperties == null) {
                    instance.rawProperties = new ConcurrentHashMap<>();
                }
                instance.rawProperties.putAll(propertyProvider.resolve(instance, instance.rawProperties));
            });
        });
    }

    /**
     * This method can be used to register new {@link PropertyProvider} at any position in the resolving chain
     *
     * @param identifier       the ID of the new {@link PropertyProvider}
     * @param propertyProvider the {@link PropertyProvider} to register
     * @param before            all the entries that should resolve after this entry, it will be registered as late as possible
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
        return register(Miapi.MiapiIdentifier(identifier), propertyProvider);
    }

    public static Map<ModuleProperty, JsonElement> merge(Map<ModuleProperty, JsonElement> old, Map<ModuleProperty, JsonElement> toMerge, MergeType mergeType) {
        toMerge.forEach(((property, element) -> {
            if (old.containsKey(property)) {
                old.put(property, property.merge(old.get(property), element, mergeType));
            } else {
                old.put(property, element);
            }
        }));
        return old;
    }

    /**
     * This interface allows other classes to add Properties to items
     */
    public interface PropertyProvider {
        Map<ModuleProperty, JsonElement> resolve(ModuleInstance moduleInstance, Map<ModuleProperty, JsonElement> oldMap);
    }
}
