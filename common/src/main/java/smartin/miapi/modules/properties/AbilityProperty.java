package smartin.miapi.modules.properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated use {@link AbilityMangerProperty} instead. it also contains context for those abilities
 */
@Deprecated
public class AbilityProperty implements ModuleProperty {
    public static final String KEY = "abilities";
    public static AbilityProperty property;

    public AbilityProperty() {
        property = this;
    }

    public static List<ItemUseAbility> get(ItemStack itemStack) {
        List<ItemUseAbility> abilities = new ArrayList<>();
        JsonElement element = ItemModule.getMergedProperty(itemStack, property);
        if (element != null) {
            element.getAsJsonArray().forEach(jsonElement -> {
                ItemUseAbility ability = ItemAbilityManager.useAbilityRegistry.get(jsonElement.getAsString());
                if (ability != null) {
                    abilities.add(0, ability);
                } else {
                    Miapi.LOGGER.error("could not resolve ability " + jsonElement.getAsString());
                }
            });
        }
        return abilities;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsJsonArray().forEach(jsonElement -> {
            assert ItemAbilityManager.useAbilityRegistry.get(jsonElement.getAsString()) != null;
        });
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case OVERWRITE -> {
                return toMerge.deepCopy();
            }
            case SMART, EXTEND -> {
                JsonArray array = old.deepCopy().getAsJsonArray();
                array.addAll(toMerge.deepCopy().getAsJsonArray());
                return Miapi.gson.toJsonTree(array);
            }
        }
        return old;
    }
}
