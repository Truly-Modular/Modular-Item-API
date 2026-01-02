package smartin.miapi.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(JsonOps.class)
public abstract class JsonOpsBooleanPatch {

    @ModifyReturnValue(
            method = "Lcom/mojang/serialization/JsonOps;getBooleanValue(Lcom/google/gson/JsonElement;)Lcom/mojang/serialization/DataResult;",
            at = @At("RETURN"),
            remap = false,
            require = -1)
    private DataResult<Boolean> miapi$allowForNBTWeirdnessConversion(DataResult<Boolean> original, final JsonElement input) {
        if (original.isError()) {
            if (input != null && input.isJsonPrimitive() && input.getAsJsonPrimitive().isNumber()) {
                int number = input.getAsInt();
                if (number == 0) {
                    return new DataResult.Success<>(false, Lifecycle.stable());
                }
                if (number == 1) {
                    return new DataResult.Success<>(true, Lifecycle.stable());
                }
            }
        }
        return original;
    }
}
