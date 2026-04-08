package smartin.miapi.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OptionalGetter<T> implements CodecBehavior.Getter<Optional<T>> {
    public static CodecBehavior.Getter<Optional> getter = (CodecBehavior.Getter<Optional>) (Object) new OptionalGetter<>();

    @Override
    @SuppressWarnings("unchecked")
    public Codec<Optional<T>> get(@Nullable Field field, Class<Optional<T>> cls, Type raw, @Nullable Type[] params, List<String> passes) {
        // Determine T from Optional<T>
        Class<T> typeClass = null;

        if (raw instanceof ParameterizedType pt && pt.getActualTypeArguments().length == 1) {
            Type tType = pt.getActualTypeArguments()[0];
            if (tType instanceof Class<?> c) {
                typeClass = (Class<T>) c;
            }
        }

        if (typeClass == null && params != null && params.length == 1 && params[0] instanceof Class<?> c) {
            typeClass = (Class<T>) c;
        }

        if (typeClass == null) {
            throw new IllegalArgumentException("Unable to determine type parameter for Optional field: " + field);
        }

        // Wrap the codec in AutoCodec and make optional
        Codec<T> baseCodec = AutoCodec.of(typeClass).codec();
        return baseCodec == null ? null : baseCodec.xmap(Optional::of, Optional::get);
    }

    @Override
    public MapCodec<Optional<T>> getSecondary(@Nullable Field field, Class<Optional<T>> cls, Type raw, @Nullable Type[] params, List<String> passes, String key) {
        // Determine T from Optional<T>
        Class<T> typeClass = null;

        if (raw instanceof ParameterizedType pt && pt.getActualTypeArguments().length == 1) {
            Type tType = pt.getActualTypeArguments()[0];
            if (tType instanceof Class<?> c) {
                typeClass = (Class<T>) c;
            }
        }

        if (typeClass == null && params != null && params.length == 1 && params[0] instanceof Class<?> c) {
            typeClass = (Class<T>) c;
        }

        if (typeClass == null) {
            throw new IllegalArgumentException("Unable to determine type parameter for Optional field: " + field);
        }

        // Wrap the codec in AutoCodec and make optional
        Codec<T> baseCodec = CodecBehavior.getCodecOrThrow(typeClass, new ArrayList<>());
        return baseCodec.optionalFieldOf(key);
    }
}