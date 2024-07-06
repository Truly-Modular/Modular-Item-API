package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.ModuleInstance;

/**
 * Simple property template with loading and caching via codecs.
 * Define a codec in the constructor(your super call), and use the {@link ModuleProperty#getData(ItemStack)} or {@link ModuleProperty#getData(ModuleInstance)} method
 * to getRaw the object you specified for that stack / ModuleInstance.
 * If you want to hold lists of data or resolve material stats, I recommend using the {@link DynamicCodecBasedProperty}.
 * However, this may still work, depending on your needs.
 *
 * @param <T> The type of object to hold
 */
public abstract class CodecBasedProperty<T> implements ModuleProperty<T> {
    protected final Codec<T> codec;

    protected CodecBasedProperty(Codec<T> codec) {
        this.codec = codec;
    }

    public T decode(JsonElement element) {
        return codec.parse(JsonOps.INSTANCE, element).getOrThrow();
    }

    public JsonElement encode(T property) {
        return codec.encode(property, JsonOps.INSTANCE, new JsonObject()).getOrThrow();
    }
}
