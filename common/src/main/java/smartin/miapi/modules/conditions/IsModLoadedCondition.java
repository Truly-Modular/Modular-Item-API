package smartin.miapi.modules.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.platform.Platform;

public class IsModLoadedCondition implements ModuleCondition {
    public static Codec<IsModLoadedCondition> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.STRING.fieldOf("mod")
                            .forGetter(condition -> condition.mod)
            ).apply(instance, IsModLoadedCondition::new));
    public String mod = "";

    public IsModLoadedCondition() {

    }

    public IsModLoadedCondition(String material) {
        this.mod = material;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        return Platform.isModLoaded(mod);
    }
}
