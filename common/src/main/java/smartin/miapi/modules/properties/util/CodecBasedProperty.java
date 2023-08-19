package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;

/**
 * Simple property template with loading and caching via codecs.
 * Define a codec in the constructor(your super call), and use the {@link CodecBasedProperty#get(ItemStack)} method
 * to get the object you specified for that stack.
 * If you want to hold lists of data or resolve material stats, I recommend using the {@link DynamicCodecBasedProperty}.
 * However, this may still work, depending on your needs.
 *
 * @param <T> The type of object to hold
 */
public abstract class CodecBasedProperty<T> implements ModuleProperty {
    private final String key;
    protected final Codec<T> codec;

    protected CodecBasedProperty(String key, Codec<T> codec) {
        this.key = key;
        this.codec = codec;

        ModularItemCache.setSupplier(key, this::createCache);
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) {
        codec.parse(JsonOps.INSTANCE, data).getOrThrow(false, s ->
                Miapi.LOGGER.error("Failed to load CodecBasedProperty! -> {}", s));
        return true;
    }

    public T get(ItemStack itemStack) {
        return (T) ModularItemCache.get(itemStack, key);
    }

    public T createCache(ItemStack stack) {
        JsonElement element = ItemModule.getMergedProperty(stack, this);
        if (element == null) {
            return null;
        }
        return codec.parse(JsonOps.INSTANCE, element).getOrThrow(false, s -> {
            Miapi.LOGGER.error("Failed to decode using codec during cache creation for a CodecBasedProperty! -> " + s);
        });
    }
}
