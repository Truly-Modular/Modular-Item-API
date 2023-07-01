package smartin.miapi.modules.properties.compat;

import com.google.gson.JsonElement;
import dev.architectury.platform.Platform;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

public class BetterCombatProperty implements ModuleProperty {
    public static String KEY = "better_combat_config";
    public static BetterCombatProperty property;

    public BetterCombatProperty() {
        property = this;
        if (Platform.isModLoaded("bettercombat")) {
            BetterCombatHelper.setup();
        }
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        if (Platform.isModLoaded("bettercombat")) {
            //BetterCombatHelper.container(data);
            return true;
        }
        return false;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        return ModuleProperty.super.merge(old, toMerge, type);
    }
}
