package smartin.miapi.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.OptionalFieldCodec;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import smartin.miapi.Miapi;

@Mixin(value = OptionalFieldCodec.class, remap = false)
public class OptionalFieldCodecMixin<A, T> {

    @Shadow
    @Final
    private String name;

    @Shadow
    @Final
    private Codec<A> elementCodec;

    @Redirect(
            method = "decode",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/serialization/DataResult;map(Ljava/util/function/Function;)Lcom/mojang/serialization/DataResult;"
            ),
            remap = false
    )
    private <A, B> DataResult<B> redirectMap(
            DataResult<A> parsed,
            java.util.function.Function<? super A, ? extends B> mapper,
            final DynamicOps<T> ops, final MapLike<T> input
    ) {
        try {
            return parsed.map(mapper);
        } catch (NullPointerException pointerException) {
            Miapi.LOGGER.warn("testing!");
            Miapi.LOGGER.error("Codec Failed! " + elementCodec.toString());
            Miapi.LOGGER.error("produced null for field " + name);
            Miapi.LOGGER.error("raw Data");
            Miapi.LOGGER.error(input.toString());

        }

        // Default behavior: proceed with the original map
        return parsed.map(mapper);
    }
}
