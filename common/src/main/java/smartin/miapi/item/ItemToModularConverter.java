package smartin.miapi.item;

import com.google.gson.reflect.TypeToken;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.ItemModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemToModularConverter implements ModularItemStackConverter.ModularConverter {
    public static List<String> pathRegexes = new ArrayList<>();
    public Map<String, ItemStack> regexes = new HashMap<>();

    static {
        pathRegexes.add("^modular_converter\\+");
    }

    public ItemToModularConverter() {
        ReloadEvents.END.subscribe(isClient -> {
            ReloadEvents.DATA_PACKS.forEach((path, data) -> {
                for (String regex : pathRegexes) {
                    if (path.matches(regex) || path.contains("modular_converter")) {
                        Map<String, ItemModule.ModuleInstance> dataMap;
                        TypeToken<Map<String, ItemModule.ModuleInstance>> token = new TypeToken<>() {
                        };

                        dataMap = Miapi.gson.fromJson(data, token.getType());

                        dataMap.forEach((itemId, moduleString) -> {
                            ItemStack stack = new ItemStack(Miapi.itemRegistry.get("miapi:modular_item"));
                            stack.getOrCreateNbt().putString("modules", moduleString.toString());
                            regexes.put(itemId, stack);
                        });
                    }
                }
            });
        });
    }

    @Override
    public ItemStack convert(ItemStack stack) {
        for (Map.Entry<String, ItemStack> entry : regexes.entrySet()) {
            if (Registry.ITEM.getId(stack.getItem()).toString().matches(entry.getKey())) {
                return entry.getValue().copy();
            }
        }
        return stack;
    }
}
