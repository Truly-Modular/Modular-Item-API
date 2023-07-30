package smartin.miapi.item;

import com.google.gson.reflect.TypeToken;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.registries.RegistryInventory;

import java.util.HashMap;
import java.util.Map;

public class ItemToModularConverter implements ModularItemStackConverter.ModularConverter {
    public Map<String, ItemStack> regexes = new HashMap<>();


    public ItemToModularConverter() {
        Miapi.registerReloadHandler(ReloadEvents.END, "modular_converter", (isClient, path, data) -> {
            Map<String, ItemModule.ModuleInstance> dataMap;
            TypeToken<Map<String, ItemModule.ModuleInstance>> token = new TypeToken<>() {
            };

            dataMap = Miapi.gson.fromJson(data, token.getType());

            dataMap.forEach((itemId, moduleString) -> {
                ItemStack stack = new ItemStack(RegistryInventory.modularItem);
                moduleString.writeToItem(stack);
                regexes.put(itemId, stack);
            });
        });
    }

    @Override
    public ItemStack convert(ItemStack stack) {
        for (Map.Entry<String, ItemStack> entry : regexes.entrySet()) {
            if (Registries.ITEM.getId(stack.getItem()).toString().matches(entry.getKey())) {
                ItemStack nextStack = entry.getValue().copy();
                nextStack.setNbt(stack.copy().getNbt());
                nextStack.getNbt().put("modules", entry.getValue().getNbt().get("modules"));
                return entry.getValue().copy();
            }
        }
        return stack;
    }
}
