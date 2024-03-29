package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * RenderProperty is a {@link ModuleProperty} to block calling the normal
 * {@link ModuleProperty#load(String, JsonElement, boolean)}
 */
public interface RenderProperty extends ModuleProperty {
    default boolean load(String moduleKey, JsonElement data, boolean isClient) throws Exception {
        return isClient && load(moduleKey, data);
    }
}
