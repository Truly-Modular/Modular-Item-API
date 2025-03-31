package smartin.miapi.modules.synergies;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.PropertyHolder;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.properties.util.EditorError;
import smartin.miapi.modules.properties.util.Validator;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;

public class ModuleSynergy extends SynergyManager.Synergy implements Validator<ModuleSynergy> {
    public static final ResourceLocation TYPE = Miapi.id("module");
    private final ResourceLocation moduleId;

    public static final MapCodec<ModuleSynergy> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Miapi.ID_CODEC.fieldOf("module").forGetter(synergy -> synergy.moduleId),
            ConditionManager.CONDITION_CODEC_DIRECT.fieldOf("condition").forGetter(synergy -> synergy.condition),
            PropertyHolder.MAP_CODEC.fieldOf("properties").forGetter(synergy -> synergy.holder)
    ).apply(instance, (moduleId, condition, holder) -> {
        ModuleSynergy synergy = new ModuleSynergy(moduleId);
        synergy.condition = condition;
        synergy.holder = holder;
        return synergy;
    }));

    public ModuleSynergy(ResourceLocation moduleId) {
        this.moduleId = moduleId;
        this.id = moduleId;
    }

    @Override
    protected ResourceLocation getType() {
        return TYPE;
    }

    @Override
    public void register() {
        SynergyManager.moduleSynergies.computeIfAbsent(moduleId, k -> new ArrayList<>()).add(this);
    }

    @Override
    public List<EditorError> validate(int line, ModuleSynergy property, boolean isClient) {
        if (RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.get(moduleId) == null) {
            return List.of(new EditorError(line, "Module " + moduleId + "was not found ! " + id, EditorError.ErrorSeverity.WARNING));
        }
        return List.of();
    }
}
