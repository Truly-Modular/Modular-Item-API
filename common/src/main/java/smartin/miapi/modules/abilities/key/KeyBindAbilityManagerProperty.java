package smartin.miapi.modules.abilities.key;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.AbilityMangerProperty;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.LinkedHashMap;
import java.util.Map;

public class KeyBindAbilityManagerProperty extends CodecProperty<Map<ResourceLocation, Map<ItemUseAbility<?>, Object>>> {
    public static String KEY = "keybind_ability_context";
    public static KeyBindAbilityManagerProperty property;
    public static Codec<Map<ResourceLocation, Map<ItemUseAbility<?>, Object>>> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, AbilityMangerProperty.CODEC);

    public KeyBindAbilityManagerProperty() {
        super(CODEC);
        property = this;
    }

    @Override
    public Map<ResourceLocation, Map<ItemUseAbility<?>, Object>> merge(Map<ResourceLocation, Map<ItemUseAbility<?>, Object>> left, Map<ResourceLocation, Map<ItemUseAbility<?>, Object>> right, MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType, (id, l, r) -> mergeInner(l, r, mergeType));
    }

    public Map<ItemUseAbility<?>, Object> mergeInner(Map<ItemUseAbility<?>, Object> left, Map<ItemUseAbility<?>, Object> right, MergeType mergeType) {
        Map<ItemUseAbility<?>, Object> merged = new LinkedHashMap<>(left);
        right.forEach(((itemUseAbility, o) -> {
            if (merged.containsKey(itemUseAbility)) {
                merged.put(itemUseAbility, mergeValues(itemUseAbility, merged.get(itemUseAbility), o, mergeType));
            } else {
                merged.put(itemUseAbility, o);
            }
        }));
        return merged;
    }

    @Override
    public Map<ResourceLocation, Map<ItemUseAbility<?>, Object>> initialize(Map<ResourceLocation, Map<ItemUseAbility<?>, Object>> map, ModuleInstance context) {
        Map<ResourceLocation, Map<ItemUseAbility<?>, Object>> init = new LinkedHashMap<>();
        map.forEach((id, property) -> {
            Map<ItemUseAbility<?>, Object> initialized = new LinkedHashMap<>();
            property.forEach((itemUseAbility, o) -> {
                initialized.put(itemUseAbility, initialize(itemUseAbility, o, context));
            });
            init.put(id, initialized);
        });
        return init;
    }

    @SuppressWarnings("unchecked")
    private <T> T initialize(ItemUseAbility<T> itemUseAbility, Object value, ModuleInstance moduleInstance) {
        return (T) itemUseAbility.initialize((T) value, moduleInstance);
    }

    @SuppressWarnings("unchecked")
    private <T> T mergeValues(ItemUseAbility<T> itemUseAbility, Object leftValue, Object rightValue, MergeType mergeType) {
        return itemUseAbility.merge((T) leftValue, (T) rightValue, mergeType);
    }
}
