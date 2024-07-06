package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.AbilityMangerProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * The Property controling {@link smartin.miapi.modules.abilities.RiptideAbility}
 * will be replaced by {@link smartin.miapi.modules.abilities.RiptideAbility}
 */
@Deprecated
public class RiptideProperty implements ModuleProperty {
    public static RiptideProperty property;
    public static final String KEY = "riptide";

    public RiptideProperty() {
        property = this;
    }


    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        new RiptideJson(data.getAsJsonObject(), new ModuleInstance(ItemModule.empty));
        return true;
    }

    public static RiptideJson getData(ItemStack itemStack) {
        AbilityMangerProperty.AbilityContext context = AbilityMangerProperty.getContext(itemStack, KEY);
        if (context != null && context.isFullContext) {
            return new RiptideJson(context.contextJson, context.contextInstance);
        }

        Tuple<ModuleInstance, JsonElement> element = property.highestPriorityJsonElement(itemStack);
        if (element != null) {
            return new RiptideJson(element.getB().getAsJsonObject(), element.getA());
        }
        return null;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case OVERWRITE -> {
                return toMerge;
            }
            case EXTEND, SMART -> {
                return old;
            }
        }
        return old;
    }

    public static class RiptideJson {
        public boolean needsWater = false;
        public boolean allowLava = false;
        public boolean needRiptideEnchant = true;
        public double riptideStrength = 3;
        public double cooldown = 0;

        public RiptideJson(JsonObject object, ModuleInstance moduleInstance) {
            needsWater = ModuleProperty.getBoolean(object, "needsWater", needsWater);
            allowLava = ModuleProperty.getBoolean(object, "allowLave", allowLava);
            needRiptideEnchant = ModuleProperty.getBoolean(object, "needRiptideEnchant", needRiptideEnchant);
            riptideStrength = ModuleProperty.getDouble(object, "riptideStrength", moduleInstance, riptideStrength);
            cooldown = ModuleProperty.getDouble(object, "cooldown", moduleInstance, cooldown);
        }
    }
}
