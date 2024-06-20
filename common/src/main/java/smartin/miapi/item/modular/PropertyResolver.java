package smartin.miapi.item.modular;

import com.google.gson.JsonElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages Properties for Items
 */
public class PropertyResolver {
    public static List<Pair<Identifier, PropertyProvider>> registry = Collections.synchronizedList(new ArrayList<>());

    /**
     * Resolves {@link ModuleProperty} maps for an {@link ItemModule.ModuleInstance}
     *
     * @param moduleInstance the {@link ItemModule.ModuleInstance} to resolve for
     * @return a map of {@link ModuleProperty} and their related Data
     */
    public static void resolve(ItemModule.ModuleInstance moduleInstance) {
        moduleInstance.allSubModules().forEach(instance -> {
            if (instance.rawProperties == null) {
                instance.rawProperties = new ConcurrentHashMap<>();
            }
        });
        registry.forEach((pair) -> {
            PropertyProvider propertyProvider = pair.getRight();
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
    public static PropertyProvider register(Identifier identifier, PropertyProvider propertyProvider, Collection<Identifier> before) {
        Pair<Identifier, PropertyProvider> entry = new Pair<>(identifier, propertyProvider);

        // Remove existing entries with the same identifier
        registry.removeIf(pair -> pair.getLeft().equals(identifier));

        // Find position to insert after identifiers
        int index = 0;
        for (Pair<Identifier, PropertyProvider> pair : registry) {
            if (before.contains(pair.getLeft())) {
                registry.add(index, entry);
                return propertyProvider;
            }
            index++;
        }

        // If after identifiers are not found, append at the end
        registry.add(entry);
        return propertyProvider;
    }

    public static PropertyProvider register(Identifier identifier, PropertyProvider propertyProvider) {
        registry.add(new Pair<>(identifier, propertyProvider));
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
        Map<ModuleProperty, JsonElement> resolve(ItemModule.ModuleInstance moduleInstance, Map<ModuleProperty, JsonElement> oldMap);
    }
}
