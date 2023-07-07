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

public class NotCondition implements ModuleCondition {
    ModuleCondition conditions;

    public NotCondition() {

    }

    public NotCondition(ModuleCondition conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean isAllowed(ItemModule.ModuleInstance moduleInstance, @Nullable BlockPos tablePos, @Nullable PlayerEntity player, Map<ModuleProperty, JsonElement> propertyMap, List<Text> reasons) {
        reasons.add(Text.literal("Just no"));
        return !conditions.isAllowed(moduleInstance, tablePos, player, propertyMap, reasons);
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new NotCondition(ConditionManager.get(element.getAsJsonObject().get("condition")));
    }
}
