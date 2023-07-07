package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;

public class OtherModuleModuleCondition implements ModuleCondition {
    public ModuleCondition condition;

    public OtherModuleModuleCondition() {
    }

    private OtherModuleModuleCondition(ModuleCondition module) {
        this.condition = module;
    }

    @Override
    public boolean isAllowed(ItemModule.ModuleInstance moduleInstance, @Nullable BlockPos tablePos, @Nullable PlayerEntity player, Map<ModuleProperty, JsonElement> propertyMap, List<Text> reasons) {
        for (ItemModule.ModuleInstance otherInstance : moduleInstance.getRoot().allSubModules()) {
            if (condition.isAllowed(otherInstance, tablePos, player, otherInstance.module.getKeyedProperties(), reasons)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new OtherModuleModuleCondition(ConditionManager.get(element.getAsJsonObject().get("condition")));
    }
}
