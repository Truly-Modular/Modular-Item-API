package smartin.miapi.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.netty.handler.codec.DecoderException;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.TagProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;

public record ItemModuleExtension(PropertyHolder holder, List<ItemModule> modules) {

    public static ItemModuleExtension loadModuleExtension(ResourceLocation path, JsonElement data, boolean isClient) {
        try {
            JsonObject moduleJson = data.getAsJsonObject();
            PropertyHolder holder = PropertyHolder.MAP_CODEC
                    .codec()
                    .decode(JsonOps.INSTANCE, moduleJson)
                    .getOrThrow((s) -> new DecoderException("Failed to decode ItemModule Extension " + path + " " + s))
                    .getFirst();
            if (moduleJson.has("tag")) {
                String tag = moduleJson.get("tag").getAsString();
                List<ItemModule> toChange = TagProperty.getModulesWithTag(tag);
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
            RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.remove(module.id());
            RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.register(module.id(), new ItemModule(module.id(), holder.applyHolder(module.properties())));
        }
    }
}
