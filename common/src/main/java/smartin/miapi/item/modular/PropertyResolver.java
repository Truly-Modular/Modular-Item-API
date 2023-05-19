package smartin.miapi.item.modular;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.MiapiRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages Properties for Items
 */
public class PropertyResolver {
    public static MiapiRegistry<PropertyProvider> propertyProviderRegistry = MiapiRegistry.getInstance(PropertyProvider.class);

    /**
     * Resolves {@link ModuleProperty} maps for an {@link ItemModule.ModuleInstance}
     *
     * @param moduleInstance the {@link ItemModule.ModuleInstance} to resolve for
     * @return a map of {@link ModuleProperty} and their related Data
     */
    public static  Map<ModuleProperty, JsonElement> resolve(ItemModule.ModuleInstance moduleInstance){
        Map<ModuleProperty, JsonElement> properties = new HashMap<>();
        propertyProviderRegistry.getFlatMap().forEach((name,propertyProvider)->{
            properties.putAll(propertyProvider.resolve(moduleInstance,properties));
        });
        return properties;
    }

    /**
     * This interface allows other classes to add Properties to items
     */
    public interface PropertyProvider{
        Map<ModuleProperty, JsonElement> resolve(ItemModule.ModuleInstance moduleInstance, Map<ModuleProperty,JsonElement> oldMap);
    }
}
