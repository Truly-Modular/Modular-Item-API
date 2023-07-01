package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotCondition implements ModuleCondition {
    ModuleCondition conditions;

    public NotCondition() {

    }

    public NotCondition(ModuleCondition conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean isAllowed(ItemModule.ModuleInstance moduleInstance, @Nullable PlayerEntity player, Map<ModuleProperty, JsonElement> propertyMap, List<Text> reasons) {
        return !conditions.isAllowed(moduleInstance, player, propertyMap, reasons);
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new NotCondition(ConditionManager.get(element.getAsJsonObject().get("condition")));
    }
}