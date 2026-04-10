package smartin.miapi.modules.synergies;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.material.properties.MaterialProperty;
import smartin.miapi.modules.PropertyHolder;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.properties.util.EditorError;
import smartin.miapi.modules.properties.util.Validator;

import java.util.ArrayList;
import java.util.List;

public class MaterialSynergy extends SynergyManager.Synergy implements Validator<MaterialSynergy> {
    public static final ResourceLocation TYPE = Miapi.id("material");
    private final ResourceLocation materialId;

    public static final MapCodec<MaterialSynergy> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Miapi.ID_CODEC.fieldOf("material").forGetter(synergy -> synergy.materialId),
            ConditionManager.CONDITION_CODEC_DIRECT.fieldOf("condition").forGetter(synergy -> synergy.condition),
            PropertyHolder.MAP_CODEC.fieldOf("properties").forGetter(synergy -> synergy.holder)
    ).apply(instance, (materialId, condition, holder) -> {
        MaterialSynergy synergy = new MaterialSynergy(materialId);
        synergy.condition = condition;
        synergy.holder = holder;
        return synergy;
    }));

    public MaterialSynergy(ResourceLocation materialId) {
        this.materialId = materialId;
        this.id = materialId;
    }

    @Override
    protected ResourceLocation getType() {
        return TYPE;
    }

    @Override
    public void register() {
        SynergyManager.materialSynergies.computeIfAbsent(materialId, k -> new ArrayList<>()).add(this);
    }

    @Override
    public List<EditorError> validate(int line, MaterialSynergy property, boolean isClient) {
        if (!MaterialProperty.MATERIAL_REGISTRY.containsKey(materialId)) {
            return List.of(new EditorError(line, "Material " + materialId + "was not found ! " + id, EditorError.ErrorSeverity.WARNING));
        }
        return List.of();
    }
}
