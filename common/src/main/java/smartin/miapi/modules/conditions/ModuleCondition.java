package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;

public interface ModuleCondition {

    boolean isAllowed(ItemModule.ModuleInstance moduleInstance, @Nullable PlayerEntity player, Map<ModuleProperty, JsonElement> propertyMap, List<Text> reasons);

    ModuleCondition load(JsonElement element);
}
