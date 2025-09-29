package smartin.miapi.mixin;


import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(DataResult.Error.class)
public class DataResultErrorMixin<R> {

    @Inject(
            method = "ap",
            at = @At("HEAD"),
            remap = false,
            cancellable = true
    )
    private <R2> void fixAp(DataResult<Function<R, R2>> functionResult,
                            CallbackInfoReturnable<DataResult.Error<R2>> cir) {
        DataResult.Error<R> self = (DataResult.Error<R>) (Object) this;
        Lifecycle combinedLifecycle = self.lifecycle().add(functionResult.lifecycle());

        if (functionResult instanceof DataResult.Success<Function<R, R2>> func) {
            cir.setReturnValue(new DataResult.Error<>(
                    self.messageSupplier(),
                    self.partialValue().map(func.value()),
                    combinedLifecycle
            ));
            return;
        }

        if (functionResult instanceof DataResult.Error<Function<R, R2>> funcError) {
            // Snapshot messages to avoid recursive supplier issues
            String leftMsg = self.messageSupplier().get();
            String rightMsg = funcError.messageSupplier().get();
            Supplier<String> safeSupplier = () -> DataResult.appendMessages(leftMsg, rightMsg);

            cir.setReturnValue(new DataResult.Error<>(
                    safeSupplier,
                    self.partialValue().flatMap(a -> funcError.partialValue().map(f -> f.apply(a))),
                    combinedLifecycle
            ));
            return;
        }

        cir.setReturnValue(new DataResult.Error<>(
                () -> "Unsupported DataResult type in ap(): " + functionResult.getClass().getName(),
                Optional.empty(),
                combinedLifecycle
        ));
    }
}