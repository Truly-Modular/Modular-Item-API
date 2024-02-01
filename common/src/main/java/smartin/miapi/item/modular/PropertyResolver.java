package smartin.miapi.item.modular;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.MiapiRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages Properties for Items
 */
public class PropertyResolver {
    /**
     * This will probably be reworked to allow for a proper prioritized ordering or sth like that to ensure certain behaviour
     * If you are an Addon-Developer reading this - be aware this might change at anytime - hit us up to tell us we should fix this
     */
    //TODO:rethink this - add a requiredPrior thing to register
    public static MiapiRegistry<PropertyProvider> propertyProviderRegistry = MiapiRegistry.getInstance(PropertyProvider.class);

    /**
     * Resolves {@link ModuleProperty} maps for an {@link ModuleInstance}
     *
     * @param moduleInstance the {@link ModuleInstance} to resolve for
     * @return a map of {@link ModuleProperty} and their related Data
     */
    public static void resolve(ModuleInstance moduleInstance) {
        moduleInstance.allSubModules().forEach(instance -> {
            if (instance.rawProperties == null) {
                instance.rawProperties = new HashMap<>();
            }
        });
        propertyProviderRegistry.getFlatMap().forEach((name, propertyProvider) -> {
            moduleInstance.allSubModules().forEach(instance -> {
                if (instance.rawProperties == null) {
                    instance.rawProperties = new HashMap<>();
                }
                instance.rawProperties.putAll(propertyProvider.resolve(instance, instance.rawProperties));
            });
        });
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
