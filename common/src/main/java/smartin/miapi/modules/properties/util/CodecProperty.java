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
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;

/**
 * Simple property template with loading and caching via codecs.
 * Define a codec in the constructor(your super call), and use the {@link ModuleProperty#getData(ItemStack)} or {@link ModuleProperty#getData(ModuleInstance)} method
 * to getRaw the object you specified for that stack / ModuleInstance.
 * However, this may still work, depending on your needs.
 *
 * @param <T> The type of object to hold
 */
public abstract class CodecProperty<T> implements ModuleProperty<T>, Validator<T> {
    protected final Codec<T> codec;
    public static RegistryOps<JsonElement> ops = RegistryOps.create(
            JsonOps.INSTANCE,
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    public static DynamicOps<JsonElement> jsonOPs = JsonOps.INSTANCE;


    public static RegistryOps<JsonElement> getOps() {
        if (Miapi.registryAccess != null) {
            return RegistryOps.create(
                    JsonOps.INSTANCE,
                    Miapi.registryAccess);
        }
        return ops;
    }

    public boolean load(ResourceLocation id, JsonElement element, boolean isClient) throws Exception {
        T decoded = decode(element);
        for (EditorError editorError : validate(0, decoded, isClient)) {
            if (editorError.severity() == EditorError.ErrorSeverity.INFO) {
                Miapi.LOGGER.info(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY.findKey(this) + " from " + id + " : " + editorError.message());
                Miapi.LOGGER.info("" + element);
            } else if (editorError.severity() == EditorError.ErrorSeverity.WARNING) {
                Miapi.LOGGER.warn(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY.findKey(this) + " from " + id + " : " + editorError.message());
                Miapi.LOGGER.warn("" + element);
            } else if (editorError.severity() == EditorError.ErrorSeverity.ERROR) {
                Miapi.LOGGER.error(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY.findKey(this) + " from " + id + " : " + editorError.message());
                Miapi.LOGGER.error("" + element);
            }
        }
        return true;
    }

    public List<EditorError> validate(int line, T property, boolean isClient) {
        return List.of();
    }


    protected CodecProperty(Codec<T> codec) {
        this.codec = codec;
    }

    public T decode(JsonElement element) {
        RegistryOps<Tag> ops = RegistryOps.create(
                NbtOps.INSTANCE,
                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        if (Miapi.registryAccess != null) {
            ops = RegistryOps.create(
                    NbtOps.INSTANCE, Miapi.registryAccess
            );
        }
        return codec.parse(
                ops, JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, element)).getOrThrow((s) ->
                new DecoderException("could not decode CodecProperty " + this.getClass().getName() + " " + s));
    }

    @Environment(EnvType.CLIENT)
    private RegistryOps<JsonElement> clientCodec() {
        return RegistryOps.create(
                JsonOps.INSTANCE,
                Minecraft.getInstance().getConnection().registryAccess());
    }


    public JsonElement encode(T property) {
        var result = codec.encodeStart(
                getOps(), property);
        if (result.isError()) {
            throw new EncoderException("Could not Encode " + this.getClass().getName() + " with Error " + result.error().toString());
        }
        return result.getOrThrow();
    }
}
