package smartin.miapi.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.conditions.ModuleCondition;

public record MaterialVariant(ModuleCondition condition, CodecMaterial overwrite) {
    public static final Codec<MaterialVariant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConditionManager.CONDITION_CODEC.fieldOf("condition").forGetter(MaterialVariant::getCondition),
            CodecMaterial.CODEC.fieldOf("overwrite").forGetter(v -> v.overwrite)
    ).apply(instance, MaterialVariant::new));

    public <T extends ModuleCondition> T getCondition() {
        return (T) condition;
    }
}
