package smartin.miapi.registries;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;

public class JsonOpsBooleanPatched extends JsonOps {
    public static JsonOpsBooleanPatched INSTANCE = new JsonOpsBooleanPatched();
    public JsonOpsBooleanPatched() {
        super(false);
    }

    @Override
    public DataResult<Boolean> getBooleanValue(final JsonElement input) {
        if (input != null && input.isJsonPrimitive() && input.getAsJsonPrimitive().isNumber()) {
            int number = input.getAsInt();
            if (number == 0) {
                return new DataResult.Success<>(false, Lifecycle.stable());
            }
            if (number == 1) {
                return new DataResult.Success<>(true, Lifecycle.stable());
            }
        }
        if (input instanceof JsonPrimitive && input.getAsJsonPrimitive().isBoolean()) {
            return DataResult.success(input.getAsBoolean());
        }
        return DataResult.error(() -> "Not a boolean: " + input);
    }
}
