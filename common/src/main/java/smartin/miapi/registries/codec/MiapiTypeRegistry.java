package smartin.miapi.registries.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple Registry like class for simple encoding and decoding by type with proper everything
 */
@SuppressWarnings("unused")
public class MiapiTypeRegistry<T> {
    private final Map<ResourceLocation, MiapiType<? extends T>> types = new LinkedHashMap<>();

    public <S extends T> MiapiType<S> register(MiapiType<S> type) {
        if (types.putIfAbsent(type.id(), type) != null) {
            throw new IllegalArgumentException("Duplicate type: " + type.id());
        }
        return type;
    }

    public <S extends T> MiapiType<S> register(ResourceLocation id, MapCodec<S> codec) {
        return register(new MiapiType<>(id, codec));
    }

    @SuppressWarnings("unchecked")
    public <S extends T> MiapiType<S> get(ResourceLocation id) {
        return (MiapiType<S>) types.get(id);
    }

    public boolean contains(ResourceLocation id) {
        return types.containsKey(id);
    }

    public Collection<MiapiType<? extends T>> values() {
        return Collections.unmodifiableCollection(types.values());
    }

    /**
     * Encodes/decodes only the type.
     */
    public Codec<MiapiType<? extends T>> typeCodec() {
        return ResourceLocation.CODEC.xmap(
                this::getOrThrow,
                MiapiType::id
        );
    }

    /**
     * Codec for a typed holder.
     */
    public Codec<MiapiHolder<? extends T>> codec() {
        return ResourceLocation.CODEC.dispatch(
                holder -> holder.type().id(),
                id -> {
                    MiapiType<? extends T> type = getOrThrow(id);
                    return holderCodec(type);
                }
        );
    }

    /**
     * MapCodec for a typed holder.
     */
    public MapCodec<MiapiHolder<? extends T>> mapCodec() {
        return codec().fieldOf("type");
    }

    @SuppressWarnings("unchecked")
    private <S extends T> MapCodec<MiapiHolder<? extends T>> holderCodec(MiapiType<S> type) {
        return type.mapCodec().xmap(
                value -> new MiapiHolder<>(type, value),
                holder -> (S) holder.value()
        );
    }

    protected MiapiType<? extends T> getOrThrow(ResourceLocation id) {
        MiapiType<? extends T> type = types.get(id);
        if (type == null) {
            throw new IllegalArgumentException("Unknown type: " + id);
        }
        return type;
    }
}