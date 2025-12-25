package smartin.miapi;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class CodecHelper {
    public static Codec<Identifier> ID_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<Identifier, T>> decode(DynamicOps<T> ops, T input) {
            Pair<String, T> result = Codec.STRING.decode(ops, input).getOrThrow(false, (s) -> {
                throw new RuntimeException("Could not Decode ID" + s);
            });
            return DataResult.success(new Pair<>(Miapi.id(result.getFirst()), result.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(Identifier input, DynamicOps<T> ops, T prefix) {
            return Codec.STRING.encode(input.toString(), ops, prefix);
        }
    };
    public static Codec<Boolean> FIXED_BOOL_CODEC = withAlternative(
            Codec.BOOL,
            Codec.INT.xmap(i -> i == 1, b -> (b ? 0 : 1)));

    public static <T> Codec<T> withAlternative(final Codec<T> primary, final Codec<? extends T> alternative) {
        return Codec.either(
                primary,
                alternative
        ).xmap(
                CodecHelper::unwrap,
                Either::left
        );
    }

    public static <U> U unwrap(final Either<? extends U, ? extends U> either) {
        return either.map(Function.identity(), Function.identity());
    }
}
