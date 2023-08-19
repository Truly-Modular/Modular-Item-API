package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.function.Predicate;

/**
 * This property controls {@link smartin.miapi.modules.abilities.CrossbowAbility}
 */
public class CrossbowProperty implements ModuleProperty {
    public static final String KEY = "Crossbow";
    public static CrossbowProperty property;

    public CrossbowProperty() {
        property = this;
    }

    public static CrossbowAbilityConfig getConfig(ItemStack itemStack) {
        return new CrossbowAbilityConfig();
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return false;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        return ModuleProperty.super.merge(old, toMerge, type);
    }

    public static class CrossbowAbilityConfig {
        public float damage = 5;
        public int pullTime = 25;
        public Predicate<ItemStack> ammoPredicate = (stack) -> {
            return stack.getItem() instanceof ArrowItem;
        };
    }
}
