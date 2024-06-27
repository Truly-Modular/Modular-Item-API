package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.MiapiRegistry;

import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ConditionManager {
    public static MiapiRegistry<ModuleCondition> moduleConditionRegistry = MiapiRegistry.getInstance(ModuleCondition.class);

    public static void setup() {
        //no onReload is required at the moment
    }

    public static ModuleCondition get(JsonElement element) {
        if (element == null) {
            return new TrueCondition();
        }
        ModuleCondition condition = moduleConditionRegistry.get(element.getAsJsonObject().get("type").getAsString());
        assert condition != null;
        return condition.load(element);
    }

    public interface ConditionContext {
        ConditionContext copy();

        List<Component> getReasons();
    }

    public static class ModuleConditionContext implements ConditionContext {

        public ModuleConditionContext(@Nullable ModuleInstance moduleInstance,
                                      @Nullable BlockPos tablePos,
                                      @Nullable Player player,
                                      @Nullable Map<ModuleProperty, JsonElement> propertyMap,
                                      List<Component> reasons) {
            this.moduleInstance = moduleInstance;
            this.tablePos = tablePos;
            this.player = player;
            this.propertyMap = propertyMap;
            this.reasons = reasons;
        }

        public @Nullable ModuleInstance moduleInstance;
        public @Nullable BlockPos tablePos;
        public @Nullable Player player;
        public @Nullable Map<ModuleProperty, JsonElement> propertyMap;
        public List<Component> reasons;

        @Override
        public ModuleConditionContext copy() {
            return new ModuleConditionContext(moduleInstance, tablePos, player, propertyMap, reasons);
        }

        @Override
        public List<Component> getReasons() {
            return reasons;
        }
    }
}
