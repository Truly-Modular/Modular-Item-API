package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
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
    public static RegistryOps<JsonElement> ops = RegistryOps.create(
            JsonOps.INSTANCE,
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    public static DynamicOps<JsonElement> jsonOPs = JsonOps.INSTANCE;


    protected CodecProperty(Codec<T> codec) {
        this.codec = codec;
    }

    public T decode(JsonElement element) {
        RegistryOps<JsonElement> ops = RegistryOps.create(
                JsonOps.INSTANCE,
                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        if (Miapi.registryAccess != null) {
            ops = RegistryOps.create(
                    JsonOps.INSTANCE, Miapi.registryAccess
            );
        } else {
            try {
                ops = clientCodec();
            } catch (RuntimeException e) {

            }
        }
        return codec.parse(
                ops, element).getOrThrow((s) -> new DecoderException("could not decode CodecProperty " + this.getClass().getName() + " " + s));
    }

    @Environment(EnvType.CLIENT)
    private RegistryOps<JsonElement> clientCodec() {
        return RegistryOps.create(
                JsonOps.INSTANCE,
                Minecraft.getInstance().getConnection().registryAccess());
    }


    public JsonElement encode(T property) {
        BuiltInRegistries.REGISTRY.asLookup();
        var result = codec.encodeStart(
                ops, property);
        if (result.isError()) {
            throw new EncoderException("Could not Encode " + this.getClass().getName() + " with Error " + result.error().toString());
        }
        return result.getOrThrow();
    }
}
