package smartin.miapi.item;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableObject;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.ItemIdProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ItemToModularConverter implements ModularItemStackConverter.ModularConverter {
    public Map<String, ItemStack> regexes = new ConcurrentHashMap<>();
    public static Codec<Map<String, ModuleInstance>> CODEC = Codec.unboundedMap(Codec.STRING, ModuleInstance.CODEC);


    public ItemToModularConverter() {
        Miapi.registerReloadHandler(ReloadEvents.MAIN, "miapi/modular_converter", regexes, (isClient, path, data) -> {
            try {
                JsonElement element = Miapi.gson.fromJson(data, JsonElement.class);
                var decoded = CODEC.decode(JsonOps.INSTANCE, element);
                if (decoded.isSuccess()) {
                    decoded.getOrThrow().getFirst().forEach((key, modules) -> {
                        ItemStack stack = new ItemStack(RegistryInventory.modularItem);
                        modules.writeToItem(stack);
                        regexes.put(key, stack);
                    });
                } else {
                    Miapi.LOGGER.error("could not read modular converter in " + path + " " + decoded.error().toString());
                }
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("Error during Modular Converter setup for " + path, e);
            }
        }, 1);

        ReloadEvents.END.subscribe(((isClient, registryAccess) -> {
            Miapi.LOGGER.info("Loaded " + regexes.size() + " Modular Converters");
        }));
    }

    public boolean preventConvert(ItemStack itemStack) {
        return false;
    }

    @Override
    public ItemStack convert(ItemStack stack) {
        if (preventConvert(stack)) {
            return stack.copy();
        }
        try {
            for (Map.Entry<String, ItemStack> entry : regexes.entrySet()) {
                if (BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().matches(entry.getKey())) {
                    ItemStack nextStack = entry.getValue().copy();
                    nextStack.applyComponents(stack.getComponents());
                    nextStack.setCount(stack.getCount());
                    MutableObject<ItemStack> mutable = new MutableObject<>(ItemIdProperty.changeId(nextStack));
                    MiapiEvents.CONVERT_ITEM.invoker().convert(stack, mutable);
                    return mutable.getValue();
                }
            }
        } catch (RuntimeException e) {
            Miapi.LOGGER.error("error during modular convertion", e);
        }
        return stack;
    }
}
