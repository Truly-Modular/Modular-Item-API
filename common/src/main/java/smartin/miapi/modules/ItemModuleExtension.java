package smartin.miapi.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.handler.codec.DecoderException;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.TagProperty;
import smartin.miapi.modules.synergies.SynergyManager;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;

public record ItemModuleExtension(SynergyManager.PropertyHolder holder, List<ItemModule> modules) {

    public static ItemModuleExtension loadModuleExtension(ResourceLocation path, JsonElement data, boolean isClient) {
        try {
            JsonObject moduleJson = data.getAsJsonObject();
            SynergyManager.PropertyHolder holder = SynergyManager.getFrom(moduleJson, isClient, path);
            if (moduleJson.has("tag")) {
                String tag = moduleJson.get("tag").getAsString();
                List<ItemModule> toChange = TagProperty.getModulesWithTag(tag);
                return new ItemModuleExtension(holder, toChange);
            } else if (moduleJson.has("id")) {
                ResourceLocation id = Miapi.id(moduleJson.get("id").getAsString());
                ItemModule module = RegistryInventory.modules.get(id);
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
            RegistryInventory.modules.remove(module.id());
            RegistryInventory.modules.register(module.id(), new ItemModule(module.id(), holder.applyHolder(module.properties())));
        }
    }
}
