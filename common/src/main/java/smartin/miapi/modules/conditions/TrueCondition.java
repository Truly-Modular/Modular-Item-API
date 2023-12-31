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

public class TrueCondition implements ModuleCondition {

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        return true;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new TrueCondition();
    }
}
