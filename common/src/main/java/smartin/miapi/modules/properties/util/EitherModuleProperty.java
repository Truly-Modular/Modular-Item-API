package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;

/**
 * A Wrapper class to allow different Datatypes pre and Post ModuleInstance initialize
 *
 * @param <T> the raw Interpretation
 * @param <K> the fully initialized PropertyData
 */
public abstract class EitherModuleProperty<T, K> implements ModuleProperty<Either<T, K>> {
    protected final Codec<T> codec;

    protected EitherModuleProperty(Codec<T> codec) {
        this.codec = codec;
    }

    protected abstract K initializeDecode(T property, ModuleInstance context);

    protected abstract K mergeInterpreted(K left, K right, MergeType mergeType);

    protected abstract T mergeRaw(T left, T right, MergeType mergeType);

    protected abstract T deInitialize(K property);

    public T decodeRaw(JsonElement element){
        return codec.decode(getOps(),element).getOrThrow(
                s -> new DecoderException("could not decode CodecProperty " + this.getClass().getName() + " " + s)).getFirst();
    }

    public JsonElement encode(Either<T, K> property) {
        T data;
        if (property.right().isPresent()) {
            data = deInitialize(property.right().get());
        } else {
            data = property.left().get();
        }
        return codec.encodeStart(getOps(), data).getOrThrow(
                s -> new DecoderException("could not encode CodecProperty " + this.getClass().getName() + " " + s));
    }

    public RegistryOps<JsonElement> getOps() {
        RegistryOps<JsonElement> ops = RegistryOps.create(
                JsonOps.INSTANCE,
                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        if (Miapi.registryAccess != null) {
            ops = RegistryOps.create(
                    JsonOps.INSTANCE, Miapi.registryAccess
            );
        }
        return ops;
    }

    @Override
    public Either<T, K> decode(JsonElement element) {
        return Either.left(decodeRaw(element));
    }

    @Override
    public Either<T, K> initialize(Either<T, K> property, ModuleInstance context) {
        return Either.right(initializeDecode(property.left().get(), context));
    }

    @Override
    public Either<T, K> merge(Either<T, K> left, Either<T, K> right, MergeType mergeType) {
        if (left.left().isPresent()) {
            return Either.left(mergeRaw(left.left().get(), right.left().get(), mergeType));
        } else {
            return Either.right(mergeInterpreted(left.right().get(), right.right().get(), mergeType));
        }
    }

}
