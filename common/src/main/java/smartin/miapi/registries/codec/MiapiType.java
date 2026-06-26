package smartin.miapi.registries.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

public final class MiapiType<T> {
    private final ResourceLocation id;
    private final MapCodec<T> codec;

    public MiapiType(ResourceLocation id, MapCodec<T> codec) {
        this.id = id;
        this.codec = codec;
    }

    public ResourceLocation id() {
        return id;
    }

    public MapCodec<T> mapCodec() {
        return codec;
    }

    public Codec<T> codec() {
        return codec.codec();
    }
}