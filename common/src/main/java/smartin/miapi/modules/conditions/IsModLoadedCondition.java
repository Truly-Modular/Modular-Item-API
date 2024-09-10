package smartin.miapi.modules.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.platform.Platform;

/**
 * @header Mod Loaded Condition
 * @description_start
 * this condition checks if a mod is loaded, you should consider using this as a Load Condition
 * @desciption_end
 * @path /data_types/condition/is_mod_loaded
 * @data type:mod_loaded
 * @data mod:the mod ID to be checked
 */
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
