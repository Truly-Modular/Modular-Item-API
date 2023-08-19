package smartin.miapi.modules.properties.compat;

import com.google.gson.JsonElement;
import dev.architectury.platform.Platform;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.AttributeProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

public class BetterCombatProperty implements ModuleProperty {
    public static String KEY = "better_combat_config";
    public static BetterCombatProperty property;

    public BetterCombatProperty() {
        property = this;
        if (Platform.isModLoaded("bettercombat")) {
            BetterCombatHelper.setup();
            AttributeProperty.attributeTransformers.add((map, itemstack) -> {
                JsonElement element = ItemModule.getMergedProperty(itemstack, property);
                if (element != null) {
                    map.removeAll(AttributeRegistry.ATTACK_RANGE);
                }
                return map;
            });
        }
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return Platform.isModLoaded("bettercombat");
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        if (type == MergeType.SMART || type == MergeType.EXTEND) {
            return toMerge;
        } else if (type == MergeType.OVERWRITE) {
            return old;
        }
        return ModuleProperty.super.merge(old, toMerge, type);
    }
}
