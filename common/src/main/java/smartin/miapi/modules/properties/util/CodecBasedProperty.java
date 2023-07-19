package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;

/**
 * Simple property template with loading and caching via codec pre-implemented.
 * Define a codec with the {@link CodecBasedProperty#codec(ItemModule.ModuleInstance)} method, and use the
 * {@link CodecBasedProperty#get(ItemStack)} method to get the object you specified for the specified stack.
 *
 * @param <T> The type of object to hold
 */
public abstract class CodecBasedProperty<T> implements ModuleProperty {
    private final String key;

    public CodecBasedProperty(String key) {
        this.key = key;

        ModularItemCache.setSupplier(key, this::createCache);
    }

    public abstract Codec<T> codec(ItemModule.ModuleInstance instance);

    @Override
    public boolean load(String moduleKey, JsonElement data) {
        codec(new ItemModule.ModuleInstance(ItemModule.empty)).parse(JsonOps.INSTANCE, data).getOrThrow(false, s ->
                Miapi.LOGGER.error("Failed to load CodecBasedProperty! -> {}", s));
        return true;
    }

    public T get(ItemStack itemStack) {
        return (T) ModularItemCache.get(itemStack, key);
    }

    public T createCache(ItemStack stack) {
        JsonElement element = ItemModule.getMergedProperty(stack, this);
        if(element == null){
            return null;
        }
        return codec(ItemModule.getModules(stack)).parse(JsonOps.INSTANCE, element).getOrThrow(false, s -> {
            Miapi.LOGGER.error("Failed to decode using codec during cache creation for a CodecBasedProperty! -> " + s);
        });
    }
}
