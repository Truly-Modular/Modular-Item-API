package smartin.miapi.client.gui.crafting.crafter.replace;

import com.google.gson.JsonElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;

import java.util.Map;
import java.util.function.Supplier;

public record CraftOption(
        ItemModule module,
        Supplier<Map<ResourceLocation, JsonElement>> data,
        double priority,
        Component displayName) {
    /**
     * simplified constructor, doesnt require a custom name, it uses the module name instead
     *
     * @param module
     * @param data
     */
    public CraftOption(ItemModule module,
                       Map<ResourceLocation, JsonElement> data, double priority) {
        this(module, () -> data, priority, new ModuleInstance(module).getModuleName());
    }
}
