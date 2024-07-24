package smartin.miapi.item;

import com.google.gson.reflect.TypeToken;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.ItemIdProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ItemToModularConverter implements ModularItemStackConverter.ModularConverter {
    public Map<String, ItemStack> regexes = new ConcurrentHashMap<>();


    public ItemToModularConverter() {
        Miapi.registerReloadHandler(ReloadEvents.MAIN, "miapi/modular_converter", regexes, (isClient, path, data) -> {
            Map<String, ModuleInstance> dataMap;
            TypeToken<Map<String, ModuleInstance>> token = new TypeToken<>() {
            };

            //TODO:reimplement convers
            /*
            dataMap = Miapi.gson.fromJson(data, token.getType());

            dataMap.forEach((itemId, moduleString) -> {
                ItemStack stack = new ItemStack(RegistryInventory.modularItem);
                moduleString.writeToItem(stack);
                regexes.put(itemId, stack);
            });

             */
        }, 1);

        ReloadEvents.END.subscribe((isClient -> {
            Miapi.LOGGER.info("Loaded " + regexes.size() + " Modular Converters");
        }));
    }

    public boolean preventConvert(ItemStack itemStack) {
        return false;
    }

    @Override
    public ItemStack convert(ItemStack stack) {
        if(preventConvert(stack)){
            return stack.copy();
        }
        for (Map.Entry<String, ItemStack> entry : regexes.entrySet()) {
            if (BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().matches(entry.getKey())) {
                ItemStack nextStack = entry.getValue().copy();
                nextStack.applyComponents(stack.getComponents());
                //TODO:add event for convertion
                nextStack.setCount(stack.getCount());
                return ItemIdProperty.changeId(nextStack);
            }
        }
        return stack;
    }
}
