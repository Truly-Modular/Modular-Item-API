package smartin.miapi.modules.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.architectury.platform.Platform;

public class IsModLoadedCondition implements ModuleCondition {
    public static Codec<ModuleCondition> CODEC = new Codec<ModuleCondition>() {
        @Override
        public <T> DataResult<Pair<ModuleCondition, T>> decode(DynamicOps<T> ops, T input) {
            Pair<String, T> result = Codec.STRING.decode(ops, ops.getMap(input).getOrThrow().get("mod")).getOrThrow();
            return DataResult.success(new Pair(new IsModLoadedCondition(result.getFirst()), result.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(ModuleCondition input, DynamicOps<T> ops, T prefix) {
            return DataResult.error(() -> "encoding condition is not fully supported");
        }
    };
    public String mod = "";

    public IsModLoadedCondition() {

    }

    public IsModLoadedCondition(String material) {
        this.mod = material;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if(Platform.isModLoaded(mod)) {
            return true;
        }
        return false;
    }
}
