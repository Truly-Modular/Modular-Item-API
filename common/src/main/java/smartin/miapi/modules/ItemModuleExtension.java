package smartin.miapi.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.handler.codec.DecoderException;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.tag.ModuleTagProperty;
import smartin.miapi.registries.JsonOpsBooleanPatched;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;
import java.util.Optional;

public record ItemModuleExtension(PropertyHolder holder, List<ItemModule> modules) {

    public static ItemModuleExtension loadModuleExtension(ResourceLocation path, JsonElement data, boolean isClient) {
        try {
            JsonObject moduleJson = data.getAsJsonObject();
            PropertyHolder holder = PropertyHolder.MAP_CODEC
                    .codec()
                    .decode(JsonOpsBooleanPatched.INSTANCE, moduleJson)
                    .getOrThrow((s) -> new DecoderException("Failed to decode ItemModule Extension " + path + " " + s))
                    .getFirst();
            if (moduleJson.has("tag")) {
                String tag = moduleJson.get("tag").getAsString();
                List<ItemModule> toChange = ModuleTagProperty.getModulesWithTag(tag);
                return new ItemModuleExtension(holder, toChange);
            } else if (moduleJson.has("id")) {
                ResourceLocation id = Miapi.id(moduleJson.get("id").getAsString());
                ItemModule module = RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.get(id);
                if (module == null) {
                    throw new DecoderException("module not found for id " + id + " by module extension " + path);
                } else {
                    return new ItemModuleExtension(holder, List.of(module));
                }
            } else {
                throw new DecoderException("module extension " + path + " did not include a id or tag.");
            }
        } catch (Exception e) {
            throw new DecoderException("Could not load Module to extend " + path, e);
        }
    }

    public void apply() {
        for (ItemModule module : modules) {
            ItemModule replaceModule = new ItemModule(module.id(), holder.applyHolder(module.properties(), Optional.empty()));
            RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.remove(module.id());
            RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.register(module.id(), replaceModule);
        }
    }
}
