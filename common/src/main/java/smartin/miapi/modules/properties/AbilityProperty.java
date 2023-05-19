package smartin.miapi.modules.properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import smartin.miapi.item.modular.ItemAbilityManager;
import smartin.miapi.item.modular.ItemUseAbility;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;

public class AbilityProperty implements ModuleProperty {
    public static final String KEY = "abilities";
    public static ModuleProperty property;

    public AbilityProperty() {
        property = this;
    }

    public static List<ItemUseAbility> get(ItemStack itemStack) {
        List<ItemUseAbility> abilities = new ArrayList<>();
        JsonElement element = ItemModule.getMergedProperty(itemStack, property);
        if(element != null){
            element.getAsJsonArray().forEach(jsonElement -> {
                abilities.add(ItemAbilityManager.useAbilityRegistry.get(jsonElement.getAsString()));
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
                return toMerge;
            }
            case SMART, EXTEND -> {
                JsonArray array = old.getAsJsonArray();
                array.addAll(toMerge.getAsJsonArray());
                return array.getAsJsonObject();
            }
        }
        return old;
    }
}
