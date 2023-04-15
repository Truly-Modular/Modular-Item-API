package smartin.miapi.item.modular;

import com.google.gson.JsonElement;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.properties.ModuleProperty;
import smartin.miapi.registries.MiapiRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages Properties for Items
 */
public class PropertyResolver {
    public static MiapiRegistry<PropertyProvider> propertyProviderRegistry = MiapiRegistry.getInstance(PropertyProvider.class);

    public static  Map<ModuleProperty, JsonElement> resolve(ItemModule.ModuleInstance moduleInstance){
        Map<ModuleProperty, JsonElement> properties = new HashMap<>();
        propertyProviderRegistry.getFlatMap().forEach((name,propertyProvider)->{
            properties.putAll(propertyProvider.resolve(moduleInstance,properties));
        });
        return properties;
    }

    public interface PropertyProvider{
        Map<ModuleProperty, JsonElement> resolve(ItemModule.ModuleInstance moduleInstance, Map<ModuleProperty,JsonElement> oldMap);
    }
}
