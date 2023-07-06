package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;

public class ChildCondition implements ModuleCondition {
    public ModuleCondition condition;

    public ChildCondition() {

    }

    private ChildCondition(ModuleCondition condition) {
        this.condition = condition;
    }

    @Override
    public boolean isAllowed(ItemModule.ModuleInstance moduleInstance, @Nullable PlayerEntity player, Map<ModuleProperty, JsonElement> propertyMap, List<Text> reasons) {
        for (ItemModule.ModuleInstance otherInstace : moduleInstance.subModules.values()) {
            assert moduleInstance.parent != null;
            if (condition.isAllowed(otherInstace, player, moduleInstance.parent.module.getKeyedProperties(), reasons)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new ChildCondition(ConditionManager.get(element.getAsJsonObject().get("condition")));
    }
}
