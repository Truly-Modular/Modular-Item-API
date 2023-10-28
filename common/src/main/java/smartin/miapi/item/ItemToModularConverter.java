package smartin.miapi.item;

import com.google.gson.reflect.TypeToken;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.EnchantmentProperty;
import smartin.miapi.modules.properties.ItemIdProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.HashMap;
import java.util.Map;

public class ItemToModularConverter implements ModularItemStackConverter.ModularConverter {
    public Map<String, ItemStack> regexes = new HashMap<>();


    public ItemToModularConverter() {
        Miapi.registerReloadHandler(ReloadEvents.MAIN, "modular_converter", regexes, (isClient, path, data) -> {
            Map<String, ItemModule.ModuleInstance> dataMap;
            TypeToken<Map<String, ItemModule.ModuleInstance>> token = new TypeToken<>() {
            };

            dataMap = Miapi.gson.fromJson(data, token.getType());

            dataMap.forEach((itemId, moduleString) -> {
                ItemStack stack = new ItemStack(RegistryInventory.modularItem);
                moduleString.writeToItem(stack);
                regexes.put(itemId, stack);
            });
        }, 1);

        ReloadEvents.END.subscribe((isClient -> {
            Miapi.LOGGER.info("Loaded " + regexes.size() + " Modular Converters");
        }));
    }

    public boolean preventConvert(ItemStack itemStack) {
        NbtCompound nbt = itemStack.getOrCreateNbt();
        if(nbt.get("CustomModelData")!=null){
            return true;
        }
        if(nbt.get("SpellboundItem")!=null){
            return true;
        }
        return false;
    }

    @Override
    public ItemStack convert(ItemStack stack) {
        if(preventConvert(stack)){
            return stack.copy();
        }
        for (Map.Entry<String, ItemStack> entry : regexes.entrySet()) {
            if (Registries.ITEM.getId(stack.getItem()).toString().matches(entry.getKey())) {
                ItemStack nextStack = entry.getValue().copy();
                nextStack.setNbt(stack.copy().getNbt());
                nextStack.getOrCreateNbt().put("modules", entry.getValue().getNbt().get("modules"));
                EnchantmentHelper.get(stack).forEach((enchantment, integer) -> {
                    if (EnchantmentProperty.isAllowed(nextStack, enchantment)) {
                        nextStack.addEnchantment(enchantment, integer);
                    } else {
                        Miapi.LOGGER.info("enchantment is not allowed");
                    }
                });
                nextStack.setCount(stack.getCount());
                return ItemIdProperty.changeId(nextStack);
            }
        }
        return stack;
    }
}
