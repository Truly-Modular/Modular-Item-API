package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AndCondition implements ModuleCondition {
    List<ModuleCondition> conditions;

    public AndCondition() {

    }

    public AndCondition(List<ModuleCondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean isAllowed(ItemModule.ModuleInstance moduleInstance, @Nullable BlockPos tablePos, @Nullable PlayerEntity player, Map<ModuleProperty, JsonElement> propertyMap, List<Text> reasons) {
        boolean isAllowed = true;
        for (ModuleCondition condition : conditions) {
            if (!condition.isAllowed(moduleInstance, tablePos, player, propertyMap, reasons)) {
                Miapi.LOGGER.warn("condition is false " + moduleInstance.module);
                isAllowed = false;
            }
        }
        return isAllowed;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        List<ModuleCondition> conditionsToLoad = new ArrayList<>();
        JsonObject object = element.getAsJsonObject();
        object.get("conditions").getAsJsonArray().forEach(subElement -> {
            conditionsToLoad.add(ConditionManager.get(subElement));
        });
        return new AndCondition(conditionsToLoad);
    }
}
