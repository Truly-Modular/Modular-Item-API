package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import smartin.miapi.modules.ModuleInstance;

/**
 * Simple property template with loading and caching via codecs.
 * Define a codec in the constructor(your super call), and use the {@link ModuleProperty#getData(ItemStack)} or {@link ModuleProperty#getData(ModuleInstance)} method
 * to getRaw the object you specified for that stack / ModuleInstance.
 * However, this may still work, depending on your needs.
 *
 * @param <T> The type of object to hold
 */
public abstract class CodecProperty<T> implements ModuleProperty<T> {
    protected final Codec<T> codec;
    //public static RegistryOps<JsonElement> ops = RegistryOps.create(
    //        JsonOps.INSTANCE,
    //        RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    public static DynamicOps<JsonElement> jsonOPs = JsonOps.INSTANCE;


    protected CodecProperty(Codec<T> codec) {
        this.codec = codec;
    }

    public T decode(JsonElement element) {
        return codec.parse(
                jsonOPs, element).getOrThrow((s) -> new DecoderException("could not decode CodecProperty " + this.getClass().getName() + " " + s));
    }

    public JsonElement encode(T property) {
        BuiltInRegistries.REGISTRY.asLookup();
        var result = codec.encodeStart(
                jsonOPs, property);
        if (result.isError()) {
            throw new EncoderException("Could not Encode " + this.getClass().getName() + " with Error " + result.error().toString());
        }
        return result.getOrThrow();
    }
}
