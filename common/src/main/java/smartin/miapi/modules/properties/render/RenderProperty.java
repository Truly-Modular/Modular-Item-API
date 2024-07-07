package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * RenderProperty is a {@link ModuleProperty} to block calling the normal
 * {@link ModuleProperty#load(String, JsonElement, boolean)}
 */
public interface RenderProperty extends ModuleProperty {
    default boolean load(ResourceLocation id, JsonElement element, boolean isClient) throws Exception {
        return isClient && load(id, element, isClient);
    }
}
