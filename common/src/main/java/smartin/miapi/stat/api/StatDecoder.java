package smartin.miapi.stat.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.datapack.ReloadHandlerBuilder;
import smartin.miapi.registries.DatapackMiapiRegistry;

public interface StatDecoder<T, M extends StatMetaData<T>> {
    DatapackMiapiRegistry<Type<?, ?>> REGISTRY =
            new DatapackMiapiRegistry<>(Type.class);

    Codec<StatDecoder<?, ?>> CODEC =
            REGISTRY.dispatchCodecTo(Type::codec);

    Stat<T, M> getStat(ResourceLocation dataId);

    Type<T, M> getType();

    final class Type<T, M extends StatMetaData<T>> {
        private final ResourceLocation id;
        private final MapCodec<? extends StatDecoder<T, M>> codec;
        static {
            ReloadHandlerBuilder
                    .builder("miapi/stat")
                    .clear(Stat.STAT_REGISTRY::clearTemporary)
                    .codec(CODEC,
                            (isClient, path, data, registryAccess) -> Stat.STAT_REGISTRY.registerTemporary(path,data.getStat(path)))
                    .register();
        }

        public Type(
                ResourceLocation id,
                MapCodec<? extends StatDecoder<T, M>> codec) {
            this.id = id;
            this.codec = codec;
        }

        public ResourceLocation id() {
            return id;
        }

        public MapCodec<? extends StatDecoder<T, M>> codec() {
            return codec;
        }
    }

    static <T, M extends StatMetaData<T>> Type<T, M> register(
            ResourceLocation id,
            MapCodec<? extends StatDecoder<T, M>> codec) {
        Type<T, M> type = new Type<>(id, codec);
        REGISTRY.register(id, type);
        return type;
    }
}