package smartin.miapi.modules.abilities.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import io.netty.handler.codec.DecoderException;

public interface CodecAbility<T> extends ItemUseAbility<T> {

    Codec<T> getCodec();

    default <K> T decode(DynamicOps<K> ops, K prefix) {
        return getCodec().decode(ops, prefix).getOrThrow(s ->
                new DecoderException("Could not decode Ability " + getClass().getName() + " with error" + s)).getFirst();
    }

    default <K> K encode(DynamicOps<K> ops, T input) {
        return getCodec().encodeStart(ops, input).getOrThrow(s ->
                new DecoderException("Could not encode Ability " + getClass().getName() + " with error" + s));
    }

    default T getDefaultContext(){
        return null;
    }
}
